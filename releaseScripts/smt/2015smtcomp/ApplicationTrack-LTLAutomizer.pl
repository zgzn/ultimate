#!/usr/bin/perl -i
#
# In the bash shell you can apply this script to all files in the folder using the
# for i in *.smt2 ; do perl THIS_SCRIPT.pl $i; done

while (<>) {
     next if $_ =~ /^\(set-option :produce.*\)/;
     next if $_ =~ /^\(get-value\s.*\)/;
     next if $_ =~ /^\(echo .*\)/;
     next if $_ =~ /^\(get-unsat-core\)/;
     next if $_ =~ /^\(set-option :timeout.*\)/;
     next if $_ =~ /^\(set-option :interpolant-check-mode.*\)/;
     next if $_ =~ /^\(set-option :proof-transformation.*\)/;
     next if $_ =~ /^\(set-option :print-success true\)/;
     next if $_ =~ /^\(exit\)/;
     next if $_ =~ /^\(assert true\)/;
  if (/^\(set-info :source .*/) {
     print "(set-info :source |\n";
  } elsif (/^\)/) {
     print "|)\n";
  } elsif (/^.*SMT script generated on.*/) {
     print 'SMT script generated by Ultimate LTL Automizer [1].
Ultimate LTL Automizer is a tool that checks if a C program satisfies an LTL 
property. The verification approch of Ultimate LTL Automizer is based on 
Büchi programs [2].

This SMT script belongs to a set of SMT scripts that was generated by applying
Ultimate LTL Automizer (revision r14204) to benchmarks that are mainly taken
from the RERS Challenge 2014 [3].

2015-05-01, Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
       and  Vincent Langenfeld (langenfv@informatik.uni-freiburg.de)


[1] https://ultimate.informatik.uni-freiburg.de/ltlautomizer
[2] Vincent Langenfeld, Daniel Dietsch, Matthias Heizmann, and Andreas Podelski
Fairness Modulo Theory: A New Approach to LTL Software Model Checking
accepted at CAV 2015
[3] The RERS Challenge 2014 http://www.rers-challenge.org/2014Isola/
';
  } else {
    print $_;
  }
}
