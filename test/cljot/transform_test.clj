(ns cljot.transform-test
  (:require [clojure.test :refer :all]
            [cljot.delta :refer :all]
            [cljot.transform :refer :all]))

(deftest op
  (testing "Transform"
    (testing "insert, insert, prioritize a"
      (let [a (-> (delta) (insert "a"))
            b (-> (delta) (insert "b"))
            e (-> (delta) (retain 1) (insert "a"))]
        (is (= e (transform-prioritising-a a b)))))

    (testing "insert, insert, prioritize b"
      (let [a (-> (delta) (insert "a"))
            b (-> (delta) (insert "b"))
            e (-> (delta) (insert "a"))]
        (is (= e (transform-prioritising-b a b)))))

    (testing "insert, retain"
      (let [a (-> (delta) (retain 1 {:bold true :size "small"}))
            b (-> (delta) (insert "b"))
            e (-> (delta) (retain 1) (retain 1 {:bold true :size "small"}))]
        (is (= e (transform-prioritising-a a b) e))
        (is (= e (transform-prioritising-b a b)))))

    (testing "insert, delete"
      (let [a (-> (delta) (delete 1))
            b (-> (delta) (insert "b"))
            e (-> (delta) (retain 1) (delete 1))]
        (is (= e (transform-prioritising-a a b)))
        (is (= e (transform-prioritising-b a b)))))

    (testing "retain, insert"
      (let [a (-> (delta) (insert "a"))
            b (-> (delta) (retain 1 {:size "big"}))
            e (-> (delta) (insert "a"))]
        (is (= e (transform-prioritising-a a b)))
        (is (= e (transform-prioritising-b a b)))))

    (testing "retain, retain, prioritize a"
      (let [a (-> (delta) (retain 1 {:bold true :size "small"}))
            b (-> (delta) (retain 1 {:size "big"}))
            e1 (-> (delta) (retain 1 {:bold true}))
            e2 (delta)]
        (is (= e1 (transform-prioritising-a a b)))
        (is (= e2 (transform-prioritising-a b a)))))

    (testing "retain, retain, prioritize b"
      (let [a (-> (delta) (retain 1 {:bold true :size "small"}))
            b (-> (delta) (retain 1 {:size "big"}))
            e1 (-> (delta) (retain 1 {:bold true :size "small"}))
            e2 (-> (delta) (retain 1 {:size "big"}))]
        (is (= e1 (transform-prioritising-b a b)))
        (is (= e2 (transform-prioritising-b b a)))))

    (testing "retain, delete"
      (let [a (-> (delta) (delete 1))
            b (-> (delta) (retain 1 {:size "big"}))
            e (-> (delta) (delete 1))]
        (is (= e (transform-prioritising-a a b)))
        (is (= e(transform-prioritising-b a b)))))

    (testing "delete, insert"
      (let [a (-> (delta) (insert "a"))
            b (-> (delta) (delete 1))
            e (-> (delta) (insert "a"))]
        (is (= e (transform-prioritising-a a b)))
        (is (= e (transform-prioritising-b a b)))))

    (testing "delete, retain"
      (let [a (-> (delta) (retain 1 {:bold true :size "small"}))
            b (-> (delta) (delete 1))
            e (delta)]
        (is (= e (transform-prioritising-a a b)))
        (is (= e (transform-prioritising-b a b)))))

    (testing "delete, delete"
      (let [a (-> (delta) (delete 1))
            b (-> (delta) (delete 1))
            e (delta)]
        (is (= e (transform-prioritising-a a b)))
        (is (= e (transform-prioritising-b a b)))))))

(deftest delta-compatibility
  (testing "Compatibility with Quill Delta
    based on:
    https://github.com/quilljs/delta/blob/2c940526a7c06e47580f15e170f90cc8c738fcbe/test/delta/transform.js"

    (testing "alternating edits"
      (let [a (-> (delta)
                  (retain 1)
                  (insert "e")
                  (delete 5)
                  (retain 1)
                  (insert "ow"))
            b (-> (delta)
                  (retain 2)
                  (insert "si")
                  (delete 5))
            e1 (-> (delta)
                   (retain 1)
                   (insert "e")
                   (delete 1)
                   (retain 2)
                   (insert "ow"))
            e2 (-> (delta)
                   (retain 2)
                   (insert "si")
                   (delete 1))]
        (is (= e1 (transform a b)))
        (is (= e2 (transform b a)))))

    (testing "conflicting appends"
      (let [a (-> (delta)
                  (retain 3)
                  (insert "aa"))
            b (-> (delta)
                  (retain 3)
                  (insert "bb"))
            e1 (-> (delta)
                   (retain 5)
                   (insert "aa"))
            e2 (-> (delta)
                   (retain 3)
                   (insert "bb"))]
        (is (= e1 (transform-prioritising-a a b)))
        (is (= e2 (transform-prioritising-b b a)))))

    (testing "prepend, append"
      (let [a (-> (delta)
                  (retain 3)
                  (insert "aa"))
            b (-> (delta)
                  (insert "bb"))
            e1 (-> (delta)
                   (retain 5)
                   (insert "aa"))
            e2 (-> (delta)
                   (insert "bb"))]
        (is (= e1 (transform a b)))
        (is (= e2 (transform b a)))))

    (testing "trailing deletes with differing lengths"
      (let [a (-> (delta)
                  (delete 3))
            b (-> (delta)
                  (retain 2)
                  (delete 1))
            e1 (-> (delta)
                   (delete 2))
            e2 (delta)]
        (is (= e1 (transform-prioritising-a a b)))
        (is (= e2 (transform-prioritising-a b a)))))

    (testing "immutability"
      (let [a1 (-> (delta) (insert "a"))
            a2 (-> (delta) (insert "a"))
            b1 (-> (delta) (insert "b"))
            b2 (-> (delta) (insert "b"))
            e (-> (delta) (retain 1) (insert "a"))]
        (is (= e (transform-prioritising-a a1 b1)))
        (is (= a1 a2))
        (is (= b1 b2))))))
