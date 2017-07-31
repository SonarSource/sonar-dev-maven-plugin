This project is deprecated. It does not bring any value, the goal `upload` did only:
* copy JAR file to $SONARQUBE/extensions/plugins
* restart server by calling web service POST api/system/restart. 

It can be easily replaced by your own script. Note that the call to api/system/restart can also be replaced by `/path/to/sonarqube/bin/{platform}/sonar restart`

### License

Copyright 2010-2017 SonarSource.

Licensed under the [GNU Lesser General Public License, Version 3.0](http://www.gnu.org/licenses/lgpl.txt)
