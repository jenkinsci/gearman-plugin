%global     plugin_name gearman-plugin
%global     debug_package %{nil}
Name:       jenkins-in-house-plugins-%{plugin_name}
Version:    0.4.1
Release:    1%{?dist}
Summary:    A jenkins in-house plugins %{plugin_name}.hpi
Obsoletes:  jenkins-upstream-plugins-%{plugin_name} <= %{version}
Requires:   jenkins
Group:      Development/Libraries
License:    BSD
URL:        https://github.com/gooddata/%{plugin_name}
Source0:    %{name}.tar.gz

BuildRequires: java
BuildRequires: maven >= 3.5.0

%description
Packaged jenkins-in-house-plugin-%{plugin_name} %{plugin_name}.hpi file

%prep
%setup -n %{name} -c 

%build
mvn versions:set -DnewVersion=%{version}
mvn versions:commit
mvn package

%install
%{__mkdir_p} %{buildroot}%{_sharedstatedir}/juseppe
%{__cp} target/%{plugin_name}.hpi %{buildroot}%{_sharedstatedir}/juseppe/

%files
%defattr(-,root,root,-)
%dir %{_sharedstatedir}/juseppe
%{_sharedstatedir}/juseppe/%{plugin_name}.hpi

%changelog
* Thu Dec 16 2021 +0700 Manh Ha <manh.ha@gooddata.com> - 0.4.1-1
- CONFIG: SETI-6576 support el8
- Change package version to 0.4.1
- Update repository url for pom.xml

* Thu Apr 23 2020 +0700 Hien Tran <hien.tran@gooddata.com> - 0.3.2-3
- CONFIG: SETI-4077 remove obsoletes package in spec file

* Wed Apr 8 2020 +0700 Hien Tran <hien.tran@gooddata.com> - 0.3.2-2
- CONFIG: SETI-4077 add gearman-plugin.spec and Makefile

* Wed Mar 25 2020 +0700 Chien Minh Do <chien.do@gooddata.com> - 0.3.2-1
- CONFIG: SETI-3633  Bump new version for gearman-plugin
