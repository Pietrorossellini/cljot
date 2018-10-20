(ns cljot.delta
  (:require [cljot.op :refer [make-insert make-retain make-delete]]
            [cljot.op-impl :refer [mergeable? merge-ops]]))

(defn- add-op [delta op]
  (if (seq delta)
    (let [last (peek delta)]
      (if (mergeable? last op)
        (conj (pop delta) (merge-ops last op))
        (conj delta op)))
    [op]))

(defn delta
  ([] [])
  ([ops]
   (if (vector? ops) (apply delta ops) [ops]))
  ([first-op & rest]
   (reduce add-op [first-op] rest)))

(defn insert
  ([delta text]
   (add-op delta (make-insert text)))
  ([delta text attributes]
   (add-op delta (make-insert text attributes))))

(defn retain
  ([delta length]
   (add-op delta (make-retain length)))
  ([delta length attributes]
   (add-op delta (make-retain length attributes))))

(defn delete
  [delta length]
  (add-op delta (make-delete length)))
