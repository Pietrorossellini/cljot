(ns cljot.delta-impl
  (:require [clojure.core.match :refer [match]]
            [cljot.op-impl :refer [mergeable? merge-ops]])
  (:import [cljot.op_impl Retain]))

(defn add-op [delta op]
  (if (seq delta)
    (let [last (peek delta)]
      (if (mergeable? last op)
        (conj (pop delta) (merge-ops last op))
        (conj delta op)))
    [op]))

(defn consume-overlap [this other rem-this rem-other]
  (let [not-nil (complement nil?)]
    (match [rem-this rem-other]
           [(rem :guard not-nil) nil] [(assoc this 0 rem) (subvec other 1)]
           [nil (rem :guard not-nil)] [(subvec this 1) (assoc other 0 rem)]
           [nil nil] [(subvec this 1) (subvec other 1)]
           [_ _] (throw (AssertionError. "Overlap should not have two remainders.")))))

(defn trim [delta]
  (let [last (peek delta)]
    (if (and
          (= (type last) Retain)
          (= (:attributes last) nil))
      (pop delta)
      delta)))
