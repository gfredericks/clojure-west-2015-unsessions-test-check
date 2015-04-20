(defproject cljs-hello-world "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :main ^:skip-aot cljs-hello-world.core
  :plugins [[lein-cljsbuild "1.0.5"]]
  :cljsbuild
  {:builds [{:source-paths ["src-cljs"]
             :compiler {:output-to "main.js"
                        :optimizations :whitespace
                        :pretty-print true}}]}
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
