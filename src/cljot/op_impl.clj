(ns cljot.op-impl)

(defprotocol Operation
  (merger [_]))

(defrecord Insert [value attributes])
(defrecord Retain [value attributes])
(defrecord Delete [value])

(extend-protocol Operation
  Insert
  (merger [_] str)
  Retain
  (merger [_] +)
  Delete
  (merger [_] +))

(defn mergeable? [this other]
  (and
    (= (type this) (type other))
    (= (:attributes this) (:attributes other))))

(defn merge-ops [this other]
  {:pre [(mergeable? this other)]}
  (let [values (map :value [this other])]
    (assoc this :value (apply (merger this) values))))
