;; (ns my-great-project.core
;;   "This namespace is CRAZY!"
;;   (:use [clojure.string :only [split join]] :reload)
;;   (:require clojure.stacktrace
;;             [clojure.test :as test]
;;             (clojure template walk) :verbose)
;;   (:import (java.util Date GregorianCalendar)))



;;             using ( var s = new RConnection( new System.Net.IPAddress( new byte[] { 192 , 168 , 37 , 10 } ) ,
;;                                              port: 6311 , user: "ruser" , password: "rpwd" ) )
;;             {
;;                 // Generate some example data
;;                 var x = Enumerable.Range( 1 , 20 ).ToArray();
;;                 var y = ( from a in x select ( 0.5 * a * a ) + 2 ).ToArray();
;;
;;                 // Build an R data frame
;;                 var d = Sexp.MakeDataFrame();
;;                 d[ "x" ] = Sexp.Make( x );
;;                 d[ "y" ] = Sexp.Make( y );
;;                 s[ "d" ] = d;
;;
;;                 // Run a linear regression, obtain the summary, and print the result
;;                 var linearModelSummary = s[ "summary(lm(y ~ x, d))" ];
;;                 Console.WriteLine( linearModelSummary.Count );
;;                 var coefs = linearModelSummary[ "coefficients" ];
;;                 var rSquared = ( double )linearModelSummary[ "r.squared" ];
;;                 Console.WriteLine( "y = {0} x + {1}. R^2 = {2,4:F}%" , coefs[ 1 , 0 ] , coefs[ 0 , 0 ] , rSquared * 100 );
;;
;;                 // Make a chart and transfer it to the local machine
;;                 s.VoidEval( "library(ggplot2)" );
;;                 s.VoidEval( "pdf(\"outfile.pdf\")" );
;;                 s.VoidEval( "print(qplot(x,y, data=d))" );
;;                 s.VoidEval( "dev.off()" );
;;
;;                 using ( var f = File.Create( "Data Plot.pdf" ) )
;;                 {
;;                     s.ReadFile( "outfile.pdf" ).CopyTo( f );
;;                 }
;;
;;                 s.RemoveFile( "outfile.pdf" );
;;
;;                 // Now let's do some linear algebra
;;                 var matA = new double[ , ] { { 14 , 9 , 3 } , { 2 , 11 , 15 } , { 0 , 12 , 17 } , { 5 , 2 , 3 } };
;;                 var matB = new double[ , ] { { 12 , 25 } , { 9 , 10 } , { 8 , 5 } };
;;                 s[ "a" ] = Sexp.Make( matA );
;;                 s[ "b" ] = Sexp.Make( matB );
;;                 Console.WriteLine( s[ "a %*% b" ].ToString() );
;;             }
;;
;;             Console.WriteLine( "Done" );
;;         }



(ns rserve-cli2.rconnection-test
  (:use [clojure.test])
  (:require [rserve-cli2.rconnection :as rc]))


(def m {:server [192 168 56 101] :port 6311 :user nil :password nil})


(deftest test-init-connection
  (testing "open an R server connection"
    (is (= "{:cursor #<RConnection RserveCLI2.RConnection>}" (str (rc/open m))))))


(def df (ref nil))

(deftest test-create-data-frame
  (testing "create an empty R data frame"
    (is (or (dosync (ref-set df (rc/sexp-make-empty-data-frame))) false))))


(deftest test-run-linear-model
  (testing "test run of a linear model"
    (let [x (rc/sexp-of-int-array (vec (range 1 21)))
          y (rc/sexp-of-double-array (vec (map #(+ 2 (* 0.5 % %)) (range 1 21))))
          _ (dosync (rc/sexp-add @df "x" x))
          _ (dosync (rc/sexp-add @df "y" y))
          _ (dosync (rc/assign "d" @df))
          lm (dosync (rc/evaluate "summary(lm(y ~ x, d))"))
          coeffs (rc/sexp-get-value-of-key lm "coefficients")
          coeff-1 (rc/sexp-get-value-of-indices 1 0)
          coeff-2 (rc/sexp-get-value-of-indices 0 0)
          r-squared (* 100.0 (rc/sexp-get-value-of-key lm "r.squared"))]
      (do (println (rc/sexp-count lm))
          (println "y = " coeff-1 " x + " coeff-2 ".")
          (println "R^2 = " r-squared "%."))
      (is (and (= coeff-1 0)
               (= coeff-2 0)
               (= r-squared 0))))))
