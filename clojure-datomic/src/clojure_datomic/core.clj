(ns clojure-datomic.core
  (:require [compojure.core :refer :all]
            [org.httpkit.server :as server]
            [ring.middleware.defaults :refer :all]
            [ring.util.response :refer [response]]))

(defn hello-handler
  [req]
  (response {:status  200
             :headers {"Content-Type" "application/json"}
             :body    {:message "Hello, World!"}}))

(defroutes app-routes
           (GET "/alive" [] hello-handler))

(defn -main
  "Main function"
  [& args]
  (let [port 3000]
    (server/run-server (wrap-defaults #'app-routes api-defaults) {:port port})
    (println (str "Running service on port " port))))
