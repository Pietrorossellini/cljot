(ns cljot.compose
  (:require [cljot.delta :refer [delta]]
            [cljot.delta.impl.delta-support :refer [add-op consume-overlap trim]]
            [cljot.delta.impl.ops-builder :as op]
            [cljot.delta.impl.ops :refer [mergeable? merge-ops merge-attributes chunk-ops]])
  (:import [cljot.delta.impl.ops Insert Retain Delete]))

(defmulti compose-ops
          (fn [a b _]
            (let [a-type (-> a first type)
                  b-type (-> b first type)]
              [a-type b-type])))

(defmethod compose-ops [Retain Delete] [a b res]
  (let [[[_ b1] [a2 b2]] (chunk-ops (first a) (first b))
        [a-next b-next] (consume-overlap a b a2 b2)]
    [a-next b-next (add-op res b1)]))

(defmethod compose-ops [::op/operation Insert] [a b res]
  [a (subvec b 1) (add-op res (first b))])

(defmethod compose-ops [Delete ::op/operation] [a b res]
  [(subvec a 1) b (add-op res (first a))])

(defmethod compose-ops [::op/operation Retain] [a b res]
  (let [[[a1 b1] [a2 b2]] (chunk-ops (first a) (first b))
        [a-next b-next] (consume-overlap a b a2 b2)]
    [a-next b-next (add-op res (merge-attributes a1 b1))]))

(defmethod compose-ops [::op/operation ::op/operation] [a b res]
  (let [[_ [a2 b2]] (chunk-ops (first a) (first b))
        [a-next b-next] (consume-overlap a b a2 b2)]
    [a-next b-next res]))

(defmethod compose-ops [nil ::op/operation] [a b res] [a (subvec b 1) (add-op res (first b))])
(defmethod compose-ops [::op/operation nil] [a b res] [(subvec a 1) b (add-op res (first a))])

(prefer-method compose-ops [::op/operation Insert] [Delete ::op/operation])
(prefer-method compose-ops [Delete ::op/operation] [::op/operation Retain])

(defn compose [a b]
  (loop [res [] a a b b]
    (if (or (seq a) (seq b))
      (let [[a-ops b-ops res] (compose-ops a b res)]
        (recur res a-ops b-ops))
      (delta (trim res)))))
