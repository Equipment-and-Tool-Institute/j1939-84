# j1939-84
This tool is an implementation of the SAE J1939-84 specifications for etools.org

This tool is used to verify that vehicles and/or components are capable of communicating a required set of information,
in accordance with the diagnostic messages specified in SAE J1939-73, to fulfill the off-board diagnostic tool interface 
requirements contained in various government regulations, including the HD OBD regulations by US EPA and
California’s Air Resources Board (ARB).

##Known Issues

* Part 1 Step 26 Doesn't handle J1939-21 DA Model Year changes
    * Some Parameter Groups change the broadcast rates or methods between model years of vehicles.  How should this be handled?
    * https://github.com/battjt/j1939-84/issues/475


* Part 1 Step 26 Handling distributed providers
    * One module (such as the Engine), may claim support for a particular SP (such as Fuel Level), but then return 
      "not available" for that SP.  The design of the vehicle intends for another OBD? module to provide the SP's value.
      Guidance needs to be provided on how to handle this.
    * https://github.com/battjt/j1939-84/issues/497

    
* Part 1 Step 3 Fails if _any_ module reports a different OBD Compliance Value
    * Currently, the tool will report a failure if any module doesn't report the same OBD Compliance Value that other
      modules report.  The intent is for all "OBD" modules to report the same OBD Compliance Values.  The documentation
      needs updating to clarify this.
    * https://github.com/battjt/j1939-84/issues/515


