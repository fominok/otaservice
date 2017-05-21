(ns otaservice.login.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [ajax.core :as ajax]
            [day8.re-frame.http-fx]))

(rf/reg-cofx
 :local-store
 (fn [coeffects local-store-key]
   (assoc coeffects
          :local-store
          (js->clj (.getItem js/localStorage local-store-key)))))

(rf/reg-event-fx
 ::login-handler
 (fn [_ [_ username password]]
   {:http-xhrio {:method :post
                 :uri  "/api/v1/login"
                 :timeout 8000
                 :params {:username username
                          :password password}
                 :format (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [::login-succeed]
                 :on-failure [::login-failed]}}))

(rf/reg-fx
 ::set-local-storage
 (fn [[key val]]
   (.setItem js/localStorage (clj->js key) val)))

(rf/reg-fx
 :print
 (fn [resp]
   (js/alert resp)))

(rf/reg-event-db
 ::logged-in
 (fn
   [db _]
   (assoc db :logged-in true)))

(rf/reg-event-fx
 ::login-succeed
 (fn [cofx [_ resp]]
   (let [val (:token resp)
         db (:db cofx)])
   {::set-local-storage [:token val]
    :dispatch [::logged-in]}))

(rf/reg-event-fx
 ::login-failed
 (fn [_ [_ resp]]
   {:print resp}))

(rf/reg-sub
 ::is-logged-in
 (fn [db _]
   (-> db
       :logged-in)))

(defn login-form []
  (let [username (reagent/atom "")
        password (reagent/atom "")]
    (fn []
      [:div.login-form
       [:input {:placeholder "username", :type "text" :value @username :on-change #(reset! username (-> % .-target .-value))}]
       [:input {:placeholder "password", :type "password" :value @password :on-change #(reset! password (-> % .-target .-value))}]
       [:div.button {:on-click #(rf/dispatch [::login-handler @username @password])} "login"]])))

(defn login-page
  []
  [:div.login-page
    [:div.form
     (if @(rf/subscribe [::is-logged-in])
       [:h2 "already logged in/stub"]
       [login-form])]])

(defn ui
  []
  [login-page])

;; -- Entry Point -------------------------------------------------------------

(defn ^:export init
  []
  #_(rf/dispatch-sync [:initialize])     ;; puts a value into application state
  (reagent/render [ui]              ;; mount the application's ui into '<div id="app" />'
                  (js/document.getElementById "app")))
