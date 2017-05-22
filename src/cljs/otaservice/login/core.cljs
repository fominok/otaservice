(ns otaservice.login.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [ajax.core :as ajax]
            [day8.re-frame.http-fx]))

;;;; Events

;; If there is a token in a localstorage, enqueue redirect effect
(rf/reg-event-fx
 :initialize
 [(rf/inject-cofx :local-store "token")]
 (fn [cofx _]
   (let [val (:local-store cofx)
         db (:db cofx)]
     (when val
       {:redirect "/"}))))

;; Build login-POST request and enqueue ajax-call effect
(rf/reg-event-fx
 :login-handler
 (fn [_ [_ username password]]
   {:http-xhrio {:method :post
                 :uri  "/api/v1/login"
                 :timeout 8000
                 :params {:username username
                          :password password}
                 :format (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:login-succeed]
                 :on-failure [:login-failed]}}))

;; Set user logged in and cause redirect
(rf/reg-event-fx
 :logged-in
 (fn [cofx _]
   (let [db (:db cofx)]
     {:db (assoc db :logged-in true)
      :redirect "/"})))

;; On successful login cause local-storage write effect
;; and dispatch logged-in event
(rf/reg-event-fx
 :login-succeed
 (fn [cofx [_ resp]]
   (let [token (:token resp)
         identity (:identity resp)
         db (:db cofx)]
     {:set-local-storage [:token token :identity identity]
      :dispatch [:logged-in]})))


;; On login failed
(rf/reg-event-db
 :login-failed
 (fn [db [_ resp]]
   (assoc db :message (get-in resp [:response :error]))))


;;;; Coeffects

;; Read localstorage
(rf/reg-cofx
 :local-store
 (fn [coeffects local-store-key]
   (assoc coeffects
          :local-store
          (js->clj (.getItem js/localStorage local-store-key)))))

;;;; Effects

;; Write localstorage
(rf/reg-fx
 :set-local-storage
 (fn [vec]
   (doseq [kv (apply hash-map vec)]
     (.setItem js/localStorage (clj->js (key kv)) (val kv)))))

;; Redirect effect
(rf/reg-fx
 :redirect
 (fn [url]
   (js/window.location.replace url)))

;;;; Subscriptions

;; Is logged in flag
(rf/reg-sub
 :is-logged-in
 (fn [db _]
   (:logged-in db)))

;; Last message from server
(rf/reg-sub
 :message
 (fn [db _]
   (:message db)))


;;;; Views

(defn login-form []
  (let [username (reagent/atom "")
        password (reagent/atom "")]
    (fn []
      [:div.login-form
       (when-let [message @(rf/subscribe [:message])]
         [:div.message message])
       [:input {:placeholder "username", :type "text" :value @username :on-change #(reset! username (-> % .-target .-value))}]
       [:input {:placeholder "password", :type "password" :value @password :on-change #(reset! password (-> % .-target .-value))}]
       [:div.button {:on-click #(rf/dispatch [:login-handler @username @password])} "login"]])))

(defn login-page
  []
  [:div.login-page
    [:div.form
     (if @(rf/subscribe [:is-logged-in])
       [:h3.message "Logged in"]
       [login-form])]])

(defn ui
  []
  [login-page])

;; -- Entry Point -------------------------------------------------------------

(defn ^:export init
  []
  (rf/dispatch-sync [:initialize])     ;; puts a value into application state
  (reagent/render [ui]              ;; mount the application's ui into '<div id="app" />'
                  (js/document.getElementById "app")))
