(ns cljot.transform
  (:require [cljot.delta :refer [delta]]
            [cljot.delta.impl.delta-support :refer [consume-overlap add-op trim]]
            [cljot.delta.impl.ops-builder :as op :refer [make-retain]]
            [cljot.delta.impl.ops :refer [chunk-ops len mergeable? merge-ops]]
            [clojure.set :refer [difference]])
  (:import [cljot.delta.impl.ops Insert Retain Delete]))

(derive ::priority-a ::priority-any)
(derive ::priority-b ::priority-any)

(defn- transform-attributes [priority a b]
  (if (= priority ::priority-b)
    a
    (update a :attributes #(let [transformed (select-keys % (difference (set (keys (:attributes a)))
                                                                        (set (keys (:attributes b)))))]
                             (if (empty? transformed) nil transformed)))))

(defmulti transform-ops
          (fn [a b _ priority]
            (let [a-type (-> a first type)
                  b-type (-> b first type)]
              [a-type b-type priority])))

(defmethod transform-ops [Insert Insert ::priority-a] [a b res _]
  (let [[[_ b1] [a2 b2]] (chunk-ops (first a) (first b))
        [_ b-next] (consume-overlap a b a2 b2)]
    [a b-next (add-op res (make-retain (len b1)))]))

(defmethod transform-ops [Insert ::op/operation ::priority-any] [a b res _]
  (let [[[a1 _] [a2 b2]] (chunk-ops (first a) (first b))
        [a-next _] (consume-overlap a b a2 b2)]
    [a-next b (add-op res a1)]))

(defmethod transform-ops [::op/operation Insert ::priority-any] [a b res _]
  (let [[[_ b1] [a2 b2]] (chunk-ops (first a) (first b))
        [_ b-next] (consume-overlap a b a2 b2)]
    [a b-next (add-op res (make-retain (len b1)))]))

(defmethod transform-ops [Delete Retain ::priority-any] [a b res _]
  (let [[[a1 _] [a2 b2]] (chunk-ops (first a) (first b))
        [a-next b-next] (consume-overlap a b a2 b2)]
    [a-next b-next (add-op res a1)]))

(defmethod transform-ops [Retain Retain ::priority-any] [a b res priority]
  (let [[[a1 b1] [a2 b2]] (chunk-ops (first a) (first b))
        [a-next b-next] (consume-overlap a b a2 b2)]
    [a-next b-next (add-op res (transform-attributes priority a1 b1))]))

(defmethod transform-ops [::op/operation Delete ::priority-any] [a b res _]
  (let [[_ [a2 b2]] (chunk-ops (first a) (first b))
        [a-next b-next] (consume-overlap a b a2 b2)]
    [a-next b-next res]))

(defmethod transform-ops [nil ::op/operation ::priority-any] [_ _ res _]
  [nil nil res])

(defmethod transform-ops [::op/operation nil ::priority-any] [a b res _]
  [(subvec a 1) b (add-op res (first a))])

(prefer-method transform-ops [Insert ::op/operation ::priority-any] [::op/operation Insert ::priority-any])
(prefer-method transform-ops [Insert ::op/operation ::priority-any] [::op/operation Delete ::priority-any])

(defn- transform-with-priority [priority a b]
  (loop [res [] a-ops a b-ops b]
    (if (or (seq a-ops) (seq b-ops))
      (let [[a-ops b-ops res] (transform-ops a-ops b-ops res priority)]
        (recur res a-ops b-ops))
      (delta (trim res)))))

(defn transform [a b] (transform-with-priority ::priority-b a b))
(defn transform-prioritising-a [a b] (transform-with-priority ::priority-a a b))
(defn transform-prioritising-b [a b] (transform-with-priority ::priority-b a b))
