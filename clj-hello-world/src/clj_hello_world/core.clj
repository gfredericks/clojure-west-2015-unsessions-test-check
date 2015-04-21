(ns clj-hello-world.core
  (:require [clojure.test.check :as t.c]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [com.gfredericks.test.chuck.generators :as gen']))

;;
;; Scalars
;;

(gen/sample gen/nat) => (0 1 1 2 4 3 4 2 8 9)

(gen/sample gen/boolean) => (false false false true true true false false false true)

(gen/sample gen/string-alphanumeric) => ("" "M" "" "33" "IO69" "2bk1I" "ek06V" "5Ip" "4tVz" "RmPS3I")

(gen/sample gen/simple-type-printable)
=>
(:x
 "}"
 -1/2
 -1
 n
 :A-:w0J-C-:_9on-:D127?:9WTi
 -2
 4
 t3i*.Q!gYe8-.aCZ+o6.?_6?!0A4.*g!.F_2!91s++.cZpH.TQZ3il9/*0*
 -6)

;;
;; Collections
;;

(gen/sample (gen/vector gen/nat)) => ([] [] [1] [] [0 3] [4 5 4] [5 5 1 5 1 5] [6 2 4 1 2 5] [7 3 3 8 6 5 3 5] [])

(-> gen/nat
    gen/vector
    gen/vector
    gen/vector
    gen/sample)
=>
([]
 []
 [[[]] [[] [2 2]]]
 [[[3 3]]]
 [[]]
 [[[2 5 5] [3 0 2 2 3]]]
 [[[4 5 5]]
  [[6 4]]
  [[2] [3 5] [5 4 3 5]]
  [[2 3 0 0 3] [] [3 5]]
  [[4 1 3 1 4 2]]
  [[5 3 1 3 5] [4 3] [3 3 0 5 4 1]]]
 [[[7 3 5] [3 5 7]]
  [[] [0 2]]
  [[4 5 7 3 6 5 2] [] [1 6 5 2 7 0]]
  []
  [[]]
  [[5 6 1 2] [5] [6 7]]]
 []
 [])

(gen/sample
 (gen/hash-map :a gen/boolean
               :b (gen/vector gen/nat)))
=>
({:a false, :b []}
 {:a true, :b []}
 {:a true, :b [1]}
 {:a true, :b [3]}
 {:a true, :b [3]}
 {:a true, :b []}
 {:a true, :b [2 2 3 1]}
 {:a false, :b [7 5 2 7 6 7 2]}
 {:a false, :b [2 8 3 4 7]}
 {:a false, :b [4 3 3 7 5 6 6 7]})


(gen/sample (gen/map gen/nat gen/string-ascii))
=>
({}
 {}
 {1 "@4"}
 {1 "7"}
 {}
 {0 "", 1 "|Q>u", 3 "["}
 {0 "G$", 1 "G?nd", 2 "ukb,:", 3 "f^ea", 4 "fW{"}
 {1 "KbCC", 2 "", 4 "k .'1R2", 5 "JB`=X", 6 "N7Q"}
 {2 "By", 4 "gR", 6 "K]UG", 7 "O", 8 "*aBS9rd"}
 {})

(gen/sample (gen/vector gen/nat 5))
=>
([0 0 0 0 0]
 [1 1 1 1 1]
 [1 2 0 1 1]
 [1 3 1 3 2]
 [1 4 3 2 3]
 [3 4 5 3 5]
 [6 3 1 3 5]
 [4 2 4 4 1]
 [6 0 2 0 3]
 [5 3 7 2 1])

;;
;; Combinators
;;

(gen/sample
 (gen/fmap #(vector % %) gen/nat)) => ([0 0] [1 1] [0 0] [2 2] [2 2] [4 4] [1 1] [7 7] [2 2] [4 4])

(gen/sample
 (gen/fmap sort (gen/vector gen/string-alphanumeric)))
=>
(()
 ()
 ()
 ("6")
 ()
 ("" "" "" "Q" "n0W6d")
 ()
 ("XFh" "f3j" "nny197" "oL9u53j")
 ("3" "t")
 ("" "7M0d" "9Y3hD" "J" "MLz822W60" "Y21qn9tz8" "e7s076u" "uEPrdCnpg"))

(gen/sample
 (gen/such-that even? gen/nat)) => (0 0 2 6 0 2 8 2 2 8)

(gen/sample
 (gen/tuple gen/boolean gen/nat))
=>
([true 0]
 [true 1]
 [false 1]
 [true 2]
 [true 1]
 [false 2]
 [false 5]
 [true 3]
 [true 1]
 [false 8])

(gen/sample
 (gen/bind (gen/tuple (gen/not-empty (gen/list (gen/hash-map :type (gen/return :person)
                                                             :name gen/string-alphanumeric
                                                             :age gen/nat)))
                      (gen/list (gen/hash-map :type (gen/return :dog)
                                              :name gen/string-alphanumeric
                                              :age gen/nat
                                              :good? gen/boolean)))
           (fn [[people dogs]]
             (gen/tuple (gen/return people)
                        (apply gen/tuple
                               (for [dog dogs]
                                 (gen/fmap #(assoc dog :owner %)
                                           (gen/elements (map :name people)))))))))

=>
([({:age 0, :name "", :type :person}) []]
 [({:age 1, :name "S", :type :person})
  [{:age 0, :good? false, :name "Y", :owner "S", :type :dog}]]
 [({:age 4, :name "Am1l", :type :person})
  [{:age 0, :good? true, :name "8f", :owner "Am1l", :type :dog}
   {:age 2, :good? false, :name "L", :owner "Am1l", :type :dog}]]
 [({:age 0, :name "4", :type :person}
   {:age 1, :name "", :type :person}
   {:age 3, :name "90", :type :person})
  [{:age 0, :good? true, :name "O", :owner "4", :type :dog}
   {:age 1, :good? false, :name "", :owner "", :type :dog}
   {:age 2, :good? true, :name "d", :owner "4", :type :dog}]]
 [({:age 2, :name "", :type :person})
  [{:age 2, :good? true, :name "Pu", :owner "", :type :dog}
   {:age 4, :good? true, :name "37", :owner "", :type :dog}
   {:age 4, :good? true, :name "4N9", :owner "", :type :dog}
   {:age 2, :good? false, :name "ZXJD", :owner "", :type :dog}]]
 [({:age 1, :name "HUlS7", :type :person}) []]
 [({:age 0, :name "f32F", :type :person}
   {:age 2, :name "0768k", :type :person}
   {:age 1, :name "03", :type :person}
   {:age 6, :name "a", :type :person}
   {:age 0, :name "3", :type :person}
   {:age 6, :name "l", :type :person})
  [{:age 6, :good? false, :name "5syfYF", :owner "3", :type :dog}]]
 [({:age 5, :name "qnzGG6", :type :person}
   {:age 1, :name "5ZsnHy", :type :person}
   {:age 6, :name "64", :type :person}
   {:age 1, :name "ijk2oI2", :type :person}
   {:age 2, :name "pksH71", :type :person}
   {:age 3, :name "C0V", :type :person}
   {:age 1, :name "dy77Kl", :type :person})
  []]
 [({:age 2, :name "lYpbv5No", :type :person}
   {:age 5, :name "I6a", :type :person})
  [{:age 2, :good? false, :name "132", :owner "I6a", :type :dog}
   {:age 2, :good? false, :name "c1", :owner "lYpbv5No", :type :dog}
   {:age 0, :good? false, :name "N32EKKB", :owner "I6a", :type :dog}]]
 [({:age 8, :name "8N", :type :person}
   {:age 7, :name "0d823kh04", :type :person}
   {:age 6, :name "o648A", :type :person}
   {:age 9, :name "mpg69X", :type :person}
   {:age 6, :name "6", :type :person}
   {:age 9, :name "d3A38S89", :type :person})
  [{:age 4, :good? false, :name "", :owner "8N", :type :dog}
   {:age 5, :good? false, :name "8r", :owner "mpg69X", :type :dog}
   {:age 0, :good? false, :name "0n6EQP", :owner "8N", :type :dog}
   {:age 7, :good? true, :name "c2p8SFtEb", :owner "o648A", :type :dog}
   {:age 3, :good? true, :name "UTo28b37", :owner "6", :type :dog}
   {:age 6, :good? false, :name "1vKg", :owner "8N", :type :dog}]])


;;
;; Properties
;;

(def prop-sorted-first-less-than-last
  (prop/for-all [v (gen/not-empty (gen/vector gen/int))]
    (let [s (sort v)]
      (< (first s) (last s)))))

(t.c/quick-check 100 prop-sorted-first-less-than-last)
=>
{:fail [[-2]],
 :failing-size 0,
 :num-tests 1,
 :result false,
 :seed 1429583361932,
 :shrunk {:depth 1, :result false, :smallest [[0]], :total-nodes-visited 3}}

;;
;; defspec for clojure.test integration
;;

(defspec prop-sorted-first-less-than-last 100
  (prop/for-all [v (gen/not-empty (gen/vector gen/int))]
    (let [s (sort v)]
      (< (first s) (last s)))))

;;
;; test.chuck's for macro
;;

(gen/sample
 (gen'/for [len gen/nat
            bools (gen/vector gen/boolean len)]
   [len bools]))
=>
([0 []]
 [1 [true]]
 [0 []]
 [3 [true true true]]
 [3 [false false true]]
 [3 [false true true]]
 [1 [false]]
 [1 [true]]
 [2 [false false]]
 [4 [true false false false]])

(def gen-person
  (gen/hash-map :type (gen/return :person)
                :name (gen'/string-from-regex #"Bob|Chuck|Garr?y")
                :age gen/nat))

(def gen-dog
  (gen/hash-map :type (gen/return :dog)
                :name gen/string-alphanumeric
                :age gen/nat
                :good? gen/boolean))

(gen/sample
 (gen'/for [:parallel [people (gen/not-empty (gen/list gen-person))
                       dogs (gen/list gen-dog)]
            :let [people-names (map :name people)]
            dog-owners (gen/vector (gen/elements people-names) (count dogs))]
   [people (map #(assoc %1 :owner %2) dogs dog-owners)]))

=>
([({:age 1, :name "Chuck", :type :person}) ()]
 [({:age 1, :name "Chuck", :type :person})
  ({:age 1, :good? false, :name "x", :owner "Chuck", :type :dog})]
 [({:age 0, :name "Gary", :type :person}) ()]
 [({:age 4, :name "Bob", :type :person}
   {:age 2, :name "Bob", :type :person}
   {:age 3, :name "Bob", :type :person}
   {:age 1, :name "Chuck", :type :person})
  ({:age 1, :good? true, :name "", :owner "Bob", :type :dog}
   {:age 3, :good? true, :name "D", :owner "Bob", :type :dog}
   {:age 3, :good? false, :name "", :owner "Bob", :type :dog})]
 [({:age 1, :name "Bob", :type :person}
   {:age 5, :name "Gary", :type :person}
   {:age 3, :name "Gary", :type :person})
  ({:age 2, :good? false, :name "", :owner "Gary", :type :dog}
   {:age 1, :good? false, :name "wR", :owner "Gary", :type :dog})]
 [({:age 2, :name "Chuck", :type :person}
   {:age 3, :name "Garry", :type :person}
   {:age 2, :name "Chuck", :type :person})
  ({:age 3, :good? true, :name "5", :owner "Chuck", :type :dog}
   {:age 3, :good? true, :name "oz4sE", :owner "Chuck", :type :dog}
   {:age 3, :good? true, :name "", :owner "Chuck", :type :dog}
   {:age 4, :good? true, :name "y", :owner "Chuck", :type :dog})]
 [({:age 2, :name "Chuck", :type :person}
   {:age 2, :name "Chuck", :type :person}
   {:age 6, :name "Chuck", :type :person})
  ({:age 2, :good? true, :name "CDmnv", :owner "Chuck", :type :dog}
   {:age 2, :good? true, :name "5dx9s4", :owner "Chuck", :type :dog}
   {:age 2, :good? true, :name "8ny", :owner "Chuck", :type :dog}
   {:age 3, :good? true, :name "86GY", :owner "Chuck", :type :dog}
   {:age 1, :good? false, :name "0nqnOc", :owner "Chuck", :type :dog}
   {:age 2, :good? true, :name "NG", :owner "Chuck", :type :dog})]
 [({:age 2, :name "Chuck", :type :person}
   {:age 5, :name "Gary", :type :person}
   {:age 5, :name "Chuck", :type :person}
   {:age 0, :name "Garry", :type :person}
   {:age 4, :name "Bob", :type :person})
  ()]
 [({:age 3, :name "Gary", :type :person}
   {:age 5, :name "Chuck", :type :person}
   {:age 3, :name "Gary", :type :person})
  ({:age 7, :good? true, :name "", :owner "Gary", :type :dog}
   {:age 5, :good? true, :name "Lu0GpxR", :owner "Chuck", :type :dog}
   {:age 3, :good? true, :name "0VJIuc", :owner "Chuck", :type :dog}
   {:age 8, :good? true, :name "4znB", :owner "Chuck", :type :dog}
   {:age 4, :good? false, :name "nn95", :owner "Gary", :type :dog}
   {:age 4, :good? true, :name "5M", :owner "Gary", :type :dog}
   {:age 3, :good? false, :name "fHoEp0U", :owner "Chuck", :type :dog}
   {:age 6, :good? false, :name "Wx", :owner "Chuck", :type :dog})]
 [({:age 4, :name "Gary", :type :person})
  ({:age 5, :good? false, :name "h", :owner "Gary", :type :dog}
   {:age 6, :good? true, :name "0iX08i1", :owner "Gary", :type :dog}
   {:age 1, :good? false, :name "3", :owner "Gary", :type :dog})])


(gen/sample gen/any)
=>
({}
 []
 []
 {() {() (), {"" -, u_*.H4.O!-Z/+_B- cwk} ()}}
 {(-2) [false], {"@D" -I!_*} {}}
 {(5/4) {:*f:hEFX "", m*-.?+l?.-.i+K-.z_gM*?/_hR :**.a6.zE80rO.U+9?98._/c+84}}
 {"«³C£ " "c¤-þ¦<"}
 {}
 ({[] ((:?HIO-:34+jEQ_:p9r!2jr8I:l_-*Q-_*5:NCP!QYj:k-!Ld3q0:w-2P*9)
       [c6Ww+5w*.-V+UN_.Z+4-7-.A!+0+8*7/_!!? 1/5])}
  {() (), ([]) {[6/7 \O] {6/7 :n-1zYv:!a?nW:n1_8RF_M:-Xj:+u+}, [4/3] {}}})
 {() ()})


(set! *print-length* 5)
(set! *print-level* 3)
