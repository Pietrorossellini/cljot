(ns cljot.compose-test
  (:require [clojure.test :refer :all]
            [cljot.compose :refer :all]
            [cljot.delta :refer :all]))

(deftest op
  (testing "Compose"
    (testing "insert, insert"
      (let [a (-> (delta) (insert "a"))
            b (-> (delta) (insert "b"))
            e (-> (delta) (insert "ba"))]
        (is (= e (compose a b)))))

    (testing "insert, retain"
      (let [a (-> (delta) (insert "a"))
            b (-> (delta) (retain 1))]
        (is (= a (compose a b)))))

    (testing "insert, retain with attributes"
      (let [a (-> (delta) (insert "a"))
            b (-> (delta) (retain 1 {:bold true :size "small"}))
            e (-> (delta) (insert "a" {:bold true :size "small"}))]
        (is (= e (compose a b)))))

    (testing "insert embed, retain with attributes"
      (let [a (-> (delta) (insert 1 {:src "image.png"}))
            b (-> (delta) (retain 1 {:alt "logo"}))
            e (-> (delta) (insert 1 {:src "image.png" :alt "logo"}))]
        (is (= e (compose a b)))))

    (testing "insert, delete all"
      (let [a (-> (delta) (insert "a"))
            b (-> (delta) (delete 1))
            e (-> (delta))]
        (is (= e (compose a b)))))

    (testing "insert, delete some"
      (let [a (-> (delta) (insert "abc"))
            b (-> (delta) (delete 1))
            e (-> (delta) (insert "bc"))]
        (is (= e (compose a b)))))

    (testing "retain, insert"
      (let [a (-> (delta) (retain 1))
            b (-> (delta) (insert "b"))
            e (-> (delta) (insert "b"))]
        (is (= e (compose a b)))))

    (testing "retain with attributes, insert"
      (let [a (-> (delta) (retain 1 {:bold true :size "small"}))
            b (-> (delta) (insert "b"))
            e (-> (delta) (insert "b") (retain 1 {:bold true :size "small"}))]
        (is (= e (compose a b)))))

    (testing "retain, retain"
      (let [a (-> (delta) (retain 1))
            b (-> (delta) (retain 1))
            e (-> (delta))]
        (is (= e (compose a b)))))

    (testing "retain with attributes, retain"
      (let [a (-> (delta) (retain 1 {:bold true :size "small"}))
            b (-> (delta) (retain 1))
            e (-> (delta) (retain 1 {:bold true :size "small"}))]
        (is (= e (compose a b)))))

    (testing "retain with attributes, retain with attributes"
      (let [a (-> (delta) (retain 1 {:bold true}))
            b (-> (delta) (retain 1 {:size "small"}))
            e (-> (delta) (retain 1 {:bold true :size "small"}))]
        (is (= e (compose a b)))))

    (testing "retain, delete"
      (let [a (-> (delta) (retain 1))
            b (-> (delta) (delete 1))
            e (-> (delta) (delete 1))]
        (is (= e (compose a b)))))

    (testing "delete, insert"
      (let [a (-> (delta) (delete 1))
            b (-> (delta) (insert "b"))
            e (-> (delta) (insert "b") (delete 1))]
        (is (= e (compose a b)))))

    (testing "delete, retain"
      (let [a (-> (delta) (delete 1))
            b (-> (delta) (retain 1))
            e (-> (delta) (delete 1))]
        (is (= e (compose a b)))))

    (testing "delete, retain with attributes"
      (let [a (-> (delta) (delete 1))
            b (-> (delta) (retain 1 {:bold true :size "small"}))
            e (-> (delta) (delete 1) (retain 1 {:bold true :size "small"}))]
        (is (= e (compose a b)))))

    (testing "delete, delete"
      (let [a (-> (delta) (delete 1))
            b (-> (delta) (delete 1))
            e (-> (delta) (delete 2))]
        (is (= e (compose a b)))))))

(deftest delta-compatibility
  (testing "Compatibility with Quill Delta
    based on:
    https://github.com/quilljs/delta/blob/2c940526a7c06e47580f15e170f90cc8c738fcbe/test/delta/compose.js"

    (testing "insert in the middle of text"
      (let [a (-> (delta) (insert "text"))
            b (-> (delta) (retain 2) (insert "_"))
            e (-> (delta) (insert "te_xt"))]
        (is (= e (compose a b)))))

    (testing "insert and delete ordering"
      (let [a (-> (delta) (insert "text"))
            b (-> (delta) (insert "text"))
            insert-first (-> (delta) (retain 2) (insert "_") (delete 1))
            delete-first (-> (delta) (retain 2) (delete 1) (insert "_"))
            e (-> (delta) (insert "te_t"))]
        (is (= e (compose a insert-first)))
        (is (= e (compose b delete-first)))))

    (testing "delete entire text"
      (let [a (-> (delta) (retain 4) (insert "delete"))
            b (-> (delta) (delete 10))
            e (-> (delta) (delete 4))]
        (is (= e (compose a b)))))

    (testing "retain more than length of text"
      (let [a (-> (delta) (insert "text"))
            b (-> (delta) (retain 10))
            e (-> (delta) (insert "text"))]
        (is (= e (compose a b)))))

    (testing "remove all attributes"
      (let [a (-> (delta) (insert "a" {:bold true}))
            b (-> (delta) (retain 1 {:bold nil}))
            e (-> (delta) (insert "a"))]
        (is (= e (compose a b)))))

    (testing "immutability"
      (let [attr1 {:bold true}
            attr2 {:bold true}
            a1 (-> (delta) (insert "Text" attr1))
            a2 (-> (delta) (insert "Text" attr2))
            b1 (-> (delta) (retain 1 {:size "small"}) (delete 2))
            b2 (-> (delta) (retain 1 {:size "small"}) (delete 2))
            e (-> (delta)
                  (insert "T" {:size "small" :bold true})
                  (insert "t" attr1))]
        (is (= e (compose a1 b1)))
        (is (= a1 a2))
        (is (= b1 b2))
        (is (= attr1 attr2))))

    (testing "retain start optimization"
      (let [a (-> (delta)
                  (insert "a" {:bold true})
                  (insert "b")
                  (insert "c" {:bold true})
                  (delete 1))
            b (-> (delta) (retain 3) (insert "d"))
            e (-> (delta)
                  (insert "a" {:bold true})
                  (insert "b")
                  (insert "c" {:bold true})
                  (insert "d")
                  (delete 1))]
        (is (= e (compose a b)))))

    (testing "retain start optimization split"
      (let [a (-> (delta)
                  (insert "a" {:bold true})
                  (insert "b")
                  (insert "c" {:bold true})
                  (retain 5)
                  (delete 1))
            b (-> (delta) (retain 4) (insert "d"))
            e (-> (delta)
                  (insert "a" {:bold true})
                  (insert "b")
                  (insert "c" {:bold true})
                  (retain 1)
                  (insert "d")
                  (retain 4)
                  (delete 1))]
        (is (= e (compose a b)))))

    (testing "retain end optimization"
      (let [a (-> (delta)
                  (insert "a" {:bold true})
                  (insert "b")
                  (insert "c" {:bold true}))
            b (-> (delta) (delete 1))
            e (-> (delta)
                  (insert "b")
                  (insert "c" {:bold true}))]
        (is (= e (compose a b)))))

    (testing "retain end optimization join"
      (let [a (-> (delta)
                  (insert "a" {:bold true})
                  (insert "b")
                  (insert "c" {:bold true})
                  (insert "d")
                  (insert "e" {:bold true})
                  (insert "f"))
            b (-> (delta) (retain 1) (delete 1))
            e (-> (delta)
                  (insert "ac" {:bold true})
                  (insert "d")
                  (insert "e" {:bold true})
                  (insert "f"))]
        (is (= e (compose a b)))))))
