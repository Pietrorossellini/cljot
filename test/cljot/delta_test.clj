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

(deftest operation
  (testing "Operation"
    (testing "Insert"
      (testing "empty text"
        (is (thrown? AssertionError (-> (delta) (insert "")))))

      (testing "text"
        (is (= [{:value "text" :attributes nil}]
               (v (-> (delta) (insert "text"))))))

      (testing "text with attributes"
        (is (= [{:value "text" :attributes {:bold true}}]
               (v (-> (delta) (insert "text" {:bold true}))))))

      (testing "empty attributes"
        (is (thrown? AssertionError (-> (delta) (insert "text" {})))))

      (testing "embed"
        (is (= [{:value 1 :attributes {:src "img.svg"}}]
               (v (-> (delta) (insert 1 {:src "img.svg"}))))))

      (testing "embed with incorrect placeholder"
        (is (thrown? AssertionError (-> (delta) (insert 2 {:src "img.svg"}))))))

    (testing "Retain"
      (testing "0"
        (is (thrown? AssertionError (-> (delta) (retain 0)))))

      (testing "1"
        (is (= [{:value 1 :attributes nil}]
               (v (-> (delta) (retain 1))))))

      (testing "n"
        (is (= [{:value 2 :attributes nil}]
               (v (-> (delta) (retain 2))))))

      (testing "with attributes"
        (is (= [{:value 1 :attributes {:bold true}}]
               (v (-> (delta) (retain 1 {:bold true})))))))

    (testing "Delete"
      (testing "0"
        (is (thrown? AssertionError (-> (delta) (delete 0)))))

      (testing "1"
        (is (= [{:value 1}]
               (v (-> (delta) (delete 1))))))

      (testing "n"
        (is (= [{:value 2}]
               (v (-> (delta) (delete 2)))))))))
