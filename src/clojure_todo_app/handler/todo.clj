(ns clojure-todo-app.handler.todo
  (:require [compojure.core :refer [defroutes context GET POST]]
            [bouncer.validators :as v]
            [clojure-todo-app.util.response :as res]
            [clojure-todo-app.view.todo :as view]
            [clojure-todo-app.util.validation :as uv]
            [clojure-todo-app.db.todo :as todo]))

(def todo-validator {:title [[v/required :message "TODOを入力してください"]]})

(defn todo-index [req]
  (let [todo-list (todo/find-todo-all)]
    (-> (view/todo-index-view req todo-list)
         res/ok
         res/html)))

(defn todo-new [req]
  (-> (view/todo-new-view req)
      res/ok
      res/html))

(defn todo-new-post [{:as req :keys [params]}]
  (uv/with-fallback #(todo-new (assoc :req :errors %))      ;エラーならtodo-newを呼び出す
    (let [params (uv/validate params todo-validator)]
      (if-let [todo (first (todo/save-todo (:title params)))]
        (-> (res/redirect (str "/todo/" (:id todo)))
            (assoc :flash {:msg "TODOを追加しました"})
            res/html)))))

(defn todo-search [req] "TODO search") ;TODO

(defn todo-show [{:as req :keys [params]}]
  (if-let [todo (todo/find-first-todo (Long/parseLong (:todo-id params)))]
    (-> (view/todo-show-view req todo)
        res/ok
        res/html)
    (res/not-found!)))

(defn todo-edit [{:as req :keys [params]}]
  (if-let [todo (todo/find-first-todo (Long/parseLong (:todo-id params)))]
    (-> (view/todo-edit-view req todo)
        res/ok
        res/html)))

(defn todo-edit-post [{:as req :keys [params]}]
  (uv/with-fallback #(todo-edit (assoc :req :errors %))
    (let [params (uv/validate params todo-validator)
          todo-id (Long/parseLong (:todo-id params))]
      (if (pos? (first (todo/update-todo todo-id (:title params))))
        (-> (res/redirect (str "/todo/" todo-id))
            (assoc :flash {:msg "TODOを更新しました"})
            res/html)))))

(defn todo-delete [{:as req :keys [params]}]
  (if-let [todo (todo/find-first-todo (Long/parseLong (:todo-id params)))]
    (-> (view/todo-delete-view req todo)
        res/ok
        res/html)))

(defn todo-delete-post [{:as req :keys [params]}]
  (let [todo-id (Long/parseLong (:todo-id params))]
    (if (pos? (first (todo/delete-todo todo-id)))
      (-> (res/redirect "/todo")
          (assoc :flash {:msg "削除しました"})
          res/html))))

(defroutes todo-routes
  (context "/todo" _
    (GET "/" _ todo-index)
    (GET "/new" _ todo-new)
    (POST "/new" _ todo-new-post)
    (GET "/search" _ todo-search)
    (context "/:todo-id"_
      (GET "/" _ todo-show)
      (GET "/edit" _ todo-edit)
      (POST "/edit" _ todo-edit-post)
      (GET "/delete" _ todo-delete)
      (POST "/delete" _ todo-delete-post))))



