all: tarball

tarball:
	git archive --format=tar HEAD | gzip > jenkins-in-house-plugins-gearman-plugin.tar.gz

.PHONY: all tarball
