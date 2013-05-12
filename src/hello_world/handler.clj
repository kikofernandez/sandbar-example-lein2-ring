(ns hello-world.handler
  (:use [compojure.core]
        [hiccup.element]
        [hiccup.core])
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [sandbar.stateful-session :as session]))

(defn layout [title counter link]
  [:div
   [:h2 (str title " Counter")]
   [:div (str "The current value of counter is " counter)]
   [:div (link-to "/" "Home")]
   [:div link]])

(defn functional-handler
  "Functional style of working with a session."
  [request]
  (let [counter (if-let [counter (-> request :session :counter)]
                  (+ counter 1)
                  1)]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (html
            (layout "Functional" counter (link-to "/stateful" "Stateful")))
     :session {:counter counter}}))

(defn stateful-handler
  "Stateful style of working with a session."
  []
  (let [counter (+ 1 (session/session-get :counter 0))]
    (do (session/session-put! :counter counter)
        (html
         (layout "Stateful" counter (link-to "/functional" "Functional"))))))

(defroutes app-routes
  (GET "/functional*" request (functional-handler request))
  (GET "/stateful*" [] (stateful-handler))
  (ANY "*" [] (html
               [:div
                [:h2 "Functional vs Stateful Session Demo"]
                [:div (link-to "/functional" "Functional")]
                [:div (link-to "/stateful" "Stateful")]]))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (handler/site app-routes)
      (session/wrap-stateful-session)))
