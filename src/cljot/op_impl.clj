(ns cljot.op-impl)

(defprotocol Operation
  (len [this])
  (split [this i])
  (merger [_]))

(defrecord Insert [value attributes])
(defrecord Retain [value attributes])
(defrecord Delete [value])

(defn- split-range [delta i]
  {:pre (< 0 i (len delta))}
  [(assoc delta :value i)
   (update delta :value #(- % i))])

(extend-protocol Operation
  Insert
    (len [this]
      (let [v (:value this)]
        (if (string? v) (count (:value this)) v)))
    (split [this i]
      {:pre (< 0 i ( len this))}
      [(update this  :value #(subs % 0 i))
       (update this  :value #(subs % i))])
    (merger [_] str)
  Retain
    (len [this] (:value this))
    (split [this i] (split-range this i))
    (merger [_] +)
  Delete
    (len [this] (:value this))
    (split [this i] (split-range this i))
    (merger [_] +))

(defn- skip-empty-attributes [op]
  (if (contains? op :attributes)
    (update
      op :attributes
      (fn [attributes]
        (if (seq attributes)
          (let [processed (into {} (filter #(identity (second %)) attributes))]
            (if (empty? processed) nil processed))
          nil)))
    op))

(defn mergeable? [this other]
  (and
    (= (type this) (type other))
    (= (:attributes this) (:attributes other))))

(defn merge-ops [this other]
  {:pre [(mergeable? this other)]}
  (let [values (map :value [this other])]
    (assoc this :value (apply (merger this) values))))

(defn merge-attributes [this other]
  (if (or (:attributes this) (:attributes other))
    (->
      (update this :attributes #(merge % (:attributes other)))
      skip-empty-attributes)
    this))

(defn chunk-ops [this other]
  (let [diff (- (len this) (len other))]
    (cond
      (zero? diff) [[this other] [nil nil]]
      (neg? diff) (let [[b1 b2] (split other (len this))] [[this b1] [nil b2]])
      (pos? diff) (let [[a1 a2] (split this (len other))] [[a1 other] [a2 nil]]))))
