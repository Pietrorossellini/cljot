(defproject cljot "0.2.0-alpha-SNAPSHOT"
  :description "Operational transformation format with support for concurrent rich text editing"
  :url "https://github.com/Pietrorossellini/cljot"
  :license {:name "EPL-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.match "0.2.2"]]
  :deploy-repositories [["clojars" {:url "https://clojars.org/repo"
                                    :username [:gpg :env/clojars_username]
                                    :password [:gpg :env/clojars_password]
                                    :sign-releases false}]])
