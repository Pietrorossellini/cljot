(ns cljot.delta.impl.ops-builder-test
  (:require [clojure.test :refer :all]
            [cljot.delta.impl.ops-builder :refer :all]))

(defn- v [op] (into {} op))

(deftest constructing
  (testing "Operation"
    (testing "Insert"
      (testing "empty text"
        (is (thrown? AssertionError (make-insert ""))))

      (testing "text"
        (is (= {:value "text" :attributes nil}
               (v (make-insert "text")))))

      (testing "text with attributes"
        (is (= {:value "text" :attributes {:bold true}}
               (v (make-insert "text" {:bold true})))))

      (testing "empty attributes"
        (is (thrown? AssertionError (make-insert "text" {}))))

      (testing "embed"
        (is (= {:value 1 :attributes {:src "img.svg"}}
               (v (make-insert 1 {:src "img.svg"})))))

      (testing "embed with incorrect placeholder"
        (is (thrown? AssertionError (make-insert 2 {:src "img.svg"})))))

    (testing "Retain"
      (testing "0"
        (is (thrown? AssertionError (make-retain 0))))

      (testing "1"
        (is (= {:value 1 :attributes nil}
               (v (make-retain 1)))))

      (testing "n"
        (is (= {:value 2 :attributes nil}
               (v (make-retain 2)))))

      (testing "with attributes"
        (is (= {:value 1 :attributes {:bold true}}
               (v (make-retain 1 {:bold true}))))))

    (testing "Delete"
      (testing "0"
        (is (thrown? AssertionError (make-delete 0))))

      (testing "1"
        (is (= {:value 1}
               (v (make-delete 1)))))

      (testing "n"
        (is (= {:value 2}
               (v (make-delete 2))))))))
