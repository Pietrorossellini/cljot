(ns cljot.delta.impl.ops-builder
  (:require [cljot.delta.impl.ops :refer :all]))

(defn- insert-text
  ([text]
   {:pre [(not (empty? text))]}
   (map->Insert {:value text}))
  ([text attributes]
   {:pre [(not (empty? text))
          (not (empty? attributes))]}
   (->Insert text attributes)))

(defn- insert-embed
  ([placeholder attributes]
   {:pre [(number? placeholder)
          (= placeholder 1)
          (not (empty? attributes))]}
   (->Insert placeholder attributes)))

(defn make-insert
  ([value] {:pre [(string? value)]} (insert-text value))
  ([value attributes]
   (cond
     (string? value) (insert-text value attributes)
     (number? value) (insert-embed value attributes))))

(defn make-retain
  ([length]
   {:pre [(pos? length)]}
   (map->Retain {:value length}))
  ([length attributes]
   {:pre [(pos? length)
          (not (empty? attributes))]}
   (->Retain length attributes)))

(defn make-delete [length]
  {:pre [(pos? length)]}
  (->Delete length))
