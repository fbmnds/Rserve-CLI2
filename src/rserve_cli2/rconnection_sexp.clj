(in-ns 'rserve-cli2.rconnection)


(defn sexp-make-data-frame
  "Makes a R data frame of given key-value list 'cols' and string list 'row-labels'."
  [& cols row-labels]
  (RserveCLI2.Sexp/MakeDataFrame cols row-labels))


;;         public static Sexp Make( object x )
;;         {
;;             if ( x is Sexp                         ) { return       ( Sexp )x;                           }
;;             if ( x is bool?                        ) { return Make( ( bool? )x                        ); }
;;             if ( x is IEnumerable<bool?>           ) { return Make( ( IEnumerable<bool?> )x           ); }
;;             if ( x is double                       ) { return Make( ( double )x                       ); }
;;             if ( x is IEnumerable<double>          ) { return Make( ( IEnumerable<double> )x          ); }
;;             if ( x is double[ , ]                  ) { return Make( ( double[ , ] )x                  ); }
;;             if ( x is decimal                      ) { return Make( ( decimal )x                      ); }
;;             if ( x is IEnumerable<decimal>         ) { return Make( ( IEnumerable<decimal> )x         ); }
;;             if ( x is decimal[ , ]                 ) { return Make( ( decimal[ , ] )x                 ); }
;;             if ( x is int                          ) { return Make( ( int )x                          ); }
;;             if ( x is IEnumerable<int>             ) { return Make( ( IEnumerable<int> )x             ); }
;;             if ( x is int[ , ]                     ) { return Make( ( int[ , ] )x                     ); }
;;             if ( x is DateTime                     ) { return Make( ( DateTime )x                     ); }
;;             if ( x is IEnumerable<DateTime>        ) { return Make( ( IEnumerable<DateTime> )x        ); }
;;             if ( x is string                       ) { return Make( ( string )x                       ); }
;;             if ( x is IEnumerable<string>          ) { return Make( ( IEnumerable<string> )x          ); }
;;             if ( x is IDictionary<string , object> ) { return Make( ( IDictionary<string , object> )x ); }
;;             throw new ArgumentException( string.Format( "no auto conversion rule for type {0} to Sexp." ,
;;                                                                                          x.GetType().Name ) );


(defn convert-vector-to-ienumerable
  ""
  [v type]
  )


(defn sexp-make
  ""
  ([x]
     (cond (or
            (= (class x) System.Boolean)
            (= (class x) System.Double)
            (= (class x) System.Decimal)
            (= (class x) System.Int32)
            (= (class x) System.Int64)
            (= (class x) System.DateTime)
            (= (class x) System.String)) (RserveCLI2.Sexp/Make x)
            () (RserveCLI2.Sexp/Make x)
            :else (throw (.ArgumentException
                          "no conversion rule for type", (str (class x))))))
  ([v type]
     (cond (and
            (vector? v)
            (= type :boolean)
            (= type :double)
            (= type :decimal)
            (= type :int)
            (= type :int32)
            (= type :int64)
            ))))





;; (last (let [r (System.Linq.Enumerable/Range (double 0) (double 10))] (to-array r)))
                                        ;

;; user=> (prn (let [r (RserveCLI2.Sexp/Make (System.Linq.Enumerable/Range (int 1) (int 10)))] r))
;; #<SexpArrayInt 1 2 3 4 5 6 7 8 9 10>
;;
;; user=> (last (let [r (RserveCLI2.Sexp/Make (System.Linq.Enumerable/Range (int 1) (int 10)))] r))
;; #<SexpArrayInt 10>
;;
;; user=> (class (last (let [r (RserveCLI2.Sexp/Make (System.Linq.Enumerable/Range (int 1) (int 10)))] r)))
;; RserveCLI2.SexpArrayInt
;;
;; user=> (class (first (let [r (RserveCLI2.Sexp/Make (System.Linq.Enumerable/Range (int 1) (int 10)))] r)))
;; RserveCLI2.SexpArrayInt
;;
;; user=> (prn (first (let [r (RserveCLI2.Sexp/Make (System.Linq.Enumerable/Range (int 1) (int 10)))] r)))
;; #<SexpArrayInt 1>
;;
;; user=> (class (let [r (RserveCLI2.Sexp/Make (System.Linq.Enumerable/Range (int 1) (int 10)))] r))
;; RserveCLI2.SexpArrayInt



(defn sexp-array-with-fn
  ([v f]
     (let [r (ref (f))]
       (dosync (doseq [s v] (.Add @r s))) r))
  ([v f convert]
     (let [r (ref (f))]
       (dosync (doseq [s v] (.Add @r (convert s))) r))))



(defn sexp-of-string-array
  [v]
  (sexp-array-with-fn v (fn [] (RserveCLI2.SexpArrayString.))))


(defn sexp-of-double-array
  [v]
  (sexp-array-with-fn v (fn [] (RserveCLI2.SexpArrayDouble.))))


;; (def r2 (sexp-of-double-array (vec (map #(double %) (range 1000000)))))
;; (last @r2)
;; DO NOT DO THIS - prints r2 = (last r2) = object information... 1 - 1000000 ...
;; (last (sexp-of-double-array (vec (map #(double %) (range 100000)))))


(defn sexp-of-decimal-array
  [v]
  (sexp-array-with-fn v (fn [] (RserveCLI2.SexpArrayDouble.)) double))


(defn sexp-of-int-as-double-array
  [v]
  (sexp-array-with-fn v (fn [] (RserveCLI2.SexpArrayDouble.)) double))


(defn sexp-of-int-array
  [v]
  (sexp-array-with-fn v (fn [] (RserveCLI2.SexpArrayInt.)) int))
