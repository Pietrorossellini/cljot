(ns cljot.delta
  (:require [cljot.op :refer [make-insert make-retain make-delete mergeable? merge-ops]]))

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
