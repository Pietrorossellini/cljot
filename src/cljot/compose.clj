(ns cljot.compose
  (:require [cljot.delta :refer [delta]]
            [cljot.delta.impl.delta-support :refer [add-op consume-overlap trim]]
            [cljot.delta.impl.ops :as ops :refer [mergeable? merge-ops merge-attributes chunk-ops]])
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

(defmethod compose-ops [::ops/operation Insert] [a b res]
  [a (subvec b 1) (add-op res (first b))])

(defmethod compose-ops [Delete ::ops/operation] [a b res]
  [(subvec a 1) b (add-op res (first a))])

(defmethod compose-ops [::ops/operation Retain] [a b res]
  (let [[[a1 b1] [a2 b2]] (chunk-ops (first a) (first b))
        [a-next b-next] (consume-overlap a b a2 b2)]
    [a-next b-next (add-op res (merge-attributes a1 b1))]))

(defmethod compose-ops [::ops/operation ::ops/operation] [a b res]
  (let [[_ [a2 b2]] (chunk-ops (first a) (first b))
        [a-next b-next] (consume-overlap a b a2 b2)]
    [a-next b-next res]))

(defmethod compose-ops [nil ::ops/operation] [a b res] [a (subvec b 1) (add-op res (first b))])
(defmethod compose-ops [::ops/operation nil] [a b res] [(subvec a 1) b (add-op res (first a))])

(prefer-method compose-ops [::ops/operation Insert] [Delete ::ops/operation])
(prefer-method compose-ops [Delete ::ops/operation] [::ops/operation Retain])

(defn compose [a b]
  (loop [res [] a a b b]
    (if (or (seq a) (seq b))
      (let [[a-ops b-ops res] (compose-ops a b res)]
        (recur res a-ops b-ops))
      (delta (trim res)))))
