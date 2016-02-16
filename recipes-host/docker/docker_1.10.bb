HOMEPAGE = "http://www.docker.com"
SUMMARY = "Linux container runtime"
SECTION = "host"
DESCRIPTION = "Linux container runtime \
 Docker complements kernel namespacing with a high-level API which \
 operates at the process level. It runs unix processes with strong \
 guarantees of isolation and repeatability across servers. \
 . \
 Docker is a great building block for automating distributed systems: \
 large-scale web deployments, database clusters, continuous deployment \
 systems, private PaaS, service-oriented architectures, etc. \
 . \
 This package contains the daemon and client. Using docker.io on non-amd64 \
 hosts is not supported at this time. Please be careful when using it \
 on anything besides amd64. \
 . \
 Also, note that kernel version 3.8 or above is required for proper \
 operation of the daemon process, and that any lower versions may have \
 subtle and/or glaring issues. \
 "

SRCREV = "590d5108bbdaabb05af590f76c9757daceb6d02e"
SRCBRANCH = "v1.10"
SRC_URI = "\
        git://github.com/docker/docker.git;branch=${SRCBRANCH};nobranch=1 \
        file://defaults \
        file://docker.service \
        file://ld-linux-x86-64.so.2 \
        file://libapparmor.so.1 \
        file://libapparmor.so.1.1.0 \
        file://libsystemd-journal.so.0 \
        file://libselinux.so.1 \
        file://libcgmanager.so.0 \
        file://libnih.so.1 \
        file://libnih-dbus.so.1 \
        file://libgcrypt.so.11 \
        file://libpcre.so.3 \
        "

# Apache-2.0 for docker
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=aadc30f9c14d876ded7bedc0afd2d3d7"

S = "${WORKDIR}/git"

DOCKER_VERSION = "1.10.0"
PV = "${DOCKER_VERSION}+git${SRCREV}"

PACKAGE_ARCH = "${MACHINE_ARCH}"
DEPENDS_append_class-target = "lvm2"
RDEPENDS_${PN} = "curl aufs-util git cgroup-lite util-linux iptables multipath-tools"
RRECOMMENDS_${PN} = "lxc docker-registry rt-tests"
RRECOMMENDS_${PN} += " kernel-module-dm-thin-pool kernel-module-nf-nat"
DOCKER_PKG="github.com/docker/docker"


# Docker doesn't have a configure stage - override default
do_configure() {
}

do_compile() {
	make cross
}

inherit systemd

SYSTEMD_PACKAGES = "${@base_contains('DISTRO_FEATURES','systemd','${PN}','',d)}"
SYSTEMD_SERVICE_${PN} = "${@base_contains('DISTRO_FEATURES','systemd','docker.service','',d)}"
SYSTEMD_SERVICE_${PN} += "${@base_contains('DISTRO_FEATURES','systemd','docker.socket','',d)}"

do_install() {
        mkdir -p ${D}/${bindir}
        cp ${S}/bundles/${DOCKER_VERSION}/dynbinary/docker-${DOCKER_VERSION} \
          ${D}/${bindir}/docker

        cp ${S}/bundles/${DOCKER_VERSION}/dynbinary/dockerinit-${DOCKER_VERSION} \
          ${D}/${bindir}/dockerinit

        if ${@base_contains('DISTRO_FEATURES','systemd','true','false',d)}; then
                install -d ${D}${systemd_unitdir}/system
                install -m 644 ${S}/contrib/init/systemd/docker.* ${D}/${systemd_unitdir}/system
                # replaces one copied from above with one that sources an environment file from /etc/default/docker
                install -m 644 ${WORKDIR}/docker.service ${D}/${systemd_unitdir}/system
                install -d ${D}/${sysconfdir}/default
                install -m 644 ${WORKDIR}/defaults ${D}/${sysconfdir}/default/docker
        fi

	mkdir -p ${D}/usr/share/docker/

	# Create glibc link
	install -d ${D}/lib64
	install -m 755 ${WORKDIR}/ld-linux-x86-64.so.2 ${D}/lib64/ld-linux-x86-64.so.2
	#ln -s -f /lib/ld-linux-x86-64.so.2 ${D}/lib64/ld-linux-x86-64.so.2

	# Manually add missing libraries (TODO: this is a nasty hack)
        install -d ${D}/usr
        install -d ${D}/usr/lib
	install -m 644 ${WORKDIR}/libapparmor.so.1.1.0 ${D}/usr/lib/libapparmor.so.1.1.0
	install -m 644 ${WORKDIR}/libapparmor.so.1 ${D}/usr/lib/libapparmor.so.1
	install -m 644 ${WORKDIR}/libsystemd-journal.so.0 ${D}/usr/lib/libsystemd-journal.so.0
	install -m 644 ${WORKDIR}/libselinux.so.1 ${D}/usr/lib/libselinux.so.1
	install -m 644 ${WORKDIR}/libcgmanager.so.0 ${D}/usr/lib/libcgmanager.so.0
	install -m 644 ${WORKDIR}/libnih.so.1 ${D}/usr/lib/libnih.so.1
	install -m 644 ${WORKDIR}/libnih-dbus.so.1 ${D}/usr/lib/libnih-dbus.so.1
	install -m 644 ${WORKDIR}/libgcrypt.so.11 ${D}/usr/lib/libgcrypt.so.11
	install -m 644 ${WORKDIR}/libpcre.so.3 ${D}/usr/lib/libpcre.so.3
}

inherit useradd
USERADD_PACKAGES = "${PN}"
GROUPADD_PARAM_${PN} = "-r docker"

FILES_${PN} += "/lib/systemd/system/*"
FILES_${PN} += "/lib64/ld-linux-x86-64.so.2"
FILES_${PN} += "/usr/lib/*"

# Skip false-positive warning:
# WARNING: QA Issue: docker: found library in wrong location: /lib64/ld-linux-x86-64.so.2 [libdir]
INSANE_SKIP_${PN} += "libdir"

# DO NOT STRIP docker and dockerinit!!!
#
# Reason:
# The "docker" package contains two binaries: "docker" and "dockerinit",
# which are both written in Go. The "dockerinit" package is built first,
# then its checksum is given to the build process compiling the "docker"
# binary. Hence the checksum of the unstripped "dockerinit" binary is hard
# coded into the "docker" binary. At runtime the "docker" binary invokes
# the "dockerinit" binary, but before doing that it ensures the checksum
# of "dockerinit" matches with the hard coded value.
#
INHIBIT_PACKAGE_STRIP = "1"

