(ns cljot.delta-test
  (:require [clojure.test :refer :all]
            [cljot.op :refer [make-insert]]
            [cljot.delta :refer :all]))

(defn- v [delta] (vec (map (partial into {}) delta)))

(deftest constructing
  (testing "Constructor"
    (testing "no arguments"
      (is (= [] (delta))))

    (testing "empty ops vector"
      (is (= [] (delta []))))

    (testing "1 op"
      (let [d (v (delta (make-insert "test" {:bold true})))
            e [{:value "test" :attributes {:bold true}}]]
        (is (= e d))))

    (testing "1 op as vector"
      (let [d (v (delta [(make-insert "test" {:bold true})]))
            e [{:value "test" :attributes {:bold true}}]]
        (is (= e d))))

    (testing "n ops"
      (let [d (v (delta (make-insert "test" {:bold true})
                        (make-insert "test")))
            e [{:value "test" :attributes {:bold true}}
               {:value "test" :attributes nil}]]
        (is (= e d))))

    (testing "n ops in vector"
      (let [d (v (delta [(make-insert "test" {:bold true})
                         (make-insert "test")]))
            e [{:value "test" :attributes {:bold true}}
               {:value "test" :attributes nil}]]
        (is (= e d))))

    (testing "mergeable ops"
      (let [d (v (delta (make-insert "test")
                        (make-insert "test")))
            e [{:value "testtest" :attributes nil}]]
        (is (= e d))))

    (testing "mergeable ops in vector"
      (let [d (v (delta [(make-insert "test")
                         (make-insert "test")]))
            e [{:value "testtest" :attributes nil}]]
        (is (= e d))))))
