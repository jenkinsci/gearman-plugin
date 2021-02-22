Overview
========
This plugin uses Gearman to support multiple Jenkins masters.

More informations can be found at https://plugins.jenkins.io/gearman-plugin/

History
=======

The plugin has been originally written by Khai Do (@zaro0508) for the OpenStack
CI infrastructure. They needed a way to scale Jenkins to multiple masters and
went with the Gearman protocol since some were familiar with it. OpenStack
eventually replaced Jenkins with Ansible and the plugin.

The Gooddata company forked it and notably added support for Jenkins Pipeline
jobs.

The Wikimedia Foundation had the use case to support Java 11. It required to
address a backware incompatible in the gearman-java library which had been
untouched since 2012.  As a commitment to the upstream Jenkins community the
old Github repository has been taken over:
* the old `master` branch has been renamed `legacy`
* The GoodData fork has been merged in
* The Java 11 incompatibility issue has been addressed

Some details can be found at https://phabricator.wikimedia.org/T271683

Legacy:
* Bugs: https://storyboard.openstack.org/#!/project/706
* Changes: https://review.opendev.org/q/project:x/gearman-plugin

Contributing
============

The canonical repository is https://github.com/jenkinsci/gearman-plugin/

License
=======

Copyright 2013 Hewlett-Packard Development Company, L.P.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

RPM build
=========
* Change the version of plugin in spec file
* Update plugin %changelog in spec file
* Rebuild rpm package with job build https://checklist.intgdc.com/job/tools/job/rpmbuild-el7-tools and merge
