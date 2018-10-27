(ns cljot.delta
  (:require [cljot.delta.impl.ops-builder :refer [make-insert make-retain make-delete]]
            [cljot.delta.impl.ops :refer [mergeable? merge-ops]]
            [cljot.delta.impl.delta-support :refer [add-op]]))

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
