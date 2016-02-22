SUMMARY = "Systemd units for StorageOS docker services"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

PR = "r0"

# Package each systemd unit file into a package of its own
OUR_SYSTEMD_PACKAGES = "${PN}-api-systemd         \
                        ${PN}-consul-systemd      \
                        ${PN}-fs-systemd          \
                        ${PN}-influxdb-systemd    \
                        ${PN}-lio-systemd         \
                        ${PN}-load-systemd        \
                        ${PN}-nats-systemd        \
                        ${PN}-registrator-systemd \
                        ${PN}-router-systemd      \
                        ${PN}-ui-systemd          \
                       "

PACKAGES = "${PN} ${OUR_SYSTEMD_PACKAGES}"

# Make the primary package 'storageos-units' have a runtime dependency on
# all the unit packages, so we don't have to include each package individually
# in the images
RDEPENDS_${PN} += "${OUR_SYSTEMD_PACKAGES}"

# All unit packages depend on docker
RDEPENDS_${PN}-api-systemd         = "docker"
RDEPENDS_${PN}-consul-systemd      = "docker"
RDEPENDS_${PN}-fs-systemd          = "docker"
RDEPENDS_${PN}-influxdb-systemd    = "docker"
RDEPENDS_${PN}-lio-systemd         = "docker"
RDEPENDS_${PN}-load-systemd        = "docker"
RDEPENDS_${PN}-nats-systemd        = "docker"
RDEPENDS_${PN}-registrator-systemd = "docker"
RDEPENDS_${PN}-router-systemd      = "docker"
RDEPENDS_${PN}-ui-systemd          = "docker"

SRC_URI = "file://api.service         \
           file://consul.service      \
           file://fs.service          \
           file://influxdb.service    \
           file://lio.service         \
           file://load.service        \
           file://nats.service        \
           file://registrator.service \
           file://router.service      \
           file://ui.service          \
           file://ca.crt              \
          "

S = "${WORKDIR}"

do_install() {
    if ${@base_contains('DISTRO_FEATURES','systemd','true','false',d)}; then
        install -d ${D}${systemd_system_unitdir}
        install -m 0644 ${WORKDIR}/*.service ${D}${systemd_system_unitdir}

        install -d ${D}${sysconfdir}/docker/certs.d/registry.reponixio.net/
        install -m 0644 ${WORKDIR}/*.crt \
	           ${D}${sysconfdir}/docker/certs.d/registry.reponixio.net/
    fi
}

# Should the services be automatically enabled; this will apply to all service
# for which there is no package override below.
SYSTEMD_AUTO_ENABLE ?= "disable"

# Service auto-enabling can be granual using overrides -- this overrides the
# above settings:
#SYSTEMD_AUTO_ENABLE_${PN}-api-systemd ?= "enable"

# These are just config files, so make it all allarch
inherit allarch systemd

SYSTEMD_PACKAGES = "${@base_contains('DISTRO_FEATURES','systemd','${OUR_SYSTEMD_PACKAGES}','',d)}"

# The systemd class uses this to generate postinst and prerm scripts
SYSTEMD_SERVICE_${PN}-api-systemd         = "${@base_contains('DISTRO_FEATURES','systemd','api.service','',d)}"
SYSTEMD_SERVICE_${PN}-consul-systemd      = "${@base_contains('DISTRO_FEATURES','systemd','consul.service','',d)}"
SYSTEMD_SERVICE_${PN}-fs-systemd          = "${@base_contains('DISTRO_FEATURES','systemd','fs.service','',d)}"
SYSTEMD_SERVICE_${PN}-influxdb-systemd    = "${@base_contains('DISTRO_FEATURES','systemd','influxdb.service','',d)}"
SYSTEMD_SERVICE_${PN}-lio-systemd         = "${@base_contains('DISTRO_FEATURES','systemd','lio.service','',d)}"
SYSTEMD_SERVICE_${PN}-load-systemd        = "${@base_contains('DISTRO_FEATURES','systemd','load.service','',d)}"
SYSTEMD_SERVICE_${PN}-nats-systemd        = "${@base_contains('DISTRO_FEATURES','systemd','nats.service','',d)}"
SYSTEMD_SERVICE_${PN}-registrator-systemd = "${@base_contains('DISTRO_FEATURES','systemd','registrator.service','',d)}"
SYSTEMD_SERVICE_${PN}-router-systemd      = "${@base_contains('DISTRO_FEATURES','systemd','router.service','',d)}"
SYSTEMD_SERVICE_${PN}-ui-systemd          = "${@base_contains('DISTRO_FEATURES','systemd','ui.service','',d)}"

# Which files go where
FILES_${PN} = "${sysconfdir}"
FILES_${PN}-api-systemd         = "${systemd_system_unitdir}/api.service"
FILES_${PN}-consul-systemd      = "${systemd_system_unitdir}/consul.service"
FILES_${PN}-fs-systemd          = "${systemd_system_unitdir}/fs.service"
FILES_${PN}-influxdb-systemd    = "${systemd_system_unitdir}/influxdb.service"
FILES_${PN}-lio-systemd         = "${systemd_system_unitdir}/lio.service"
FILES_${PN}-load-systemd        = "${systemd_system_unitdir}/load.service"
FILES_${PN}-nats-systemd        = "${systemd_system_unitdir}/nats.service"
FILES_${PN}-registrator-systemd = "${systemd_system_unitdir}/registrator.service"
FILES_${PN}-router-systemd      = "${systemd_system_unitdir}/router.service"
FILES_${PN}-ui-systemd          = "${systemd_system_unitdir}/ui.service"
