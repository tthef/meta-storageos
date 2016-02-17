DESCRIPTION = "\
  Go is an open source programming language that makes it easy to build simple, \
  reliable, and efficient software. \
  "
HOMEPAGE = "https://golang.org/"
LICENSE = "BSD-3-Clause"

DEPENDS = "virtual/${TARGET_PREFIX}gcc"

SRC_URI = "http://golang.org/dl/go${PV}.src.tar.gz"

S = "${WORKDIR}/go/"

inherit cross

LIC_FILES_CHKSUM = "file://LICENSE;md5=591778525c869cdde0ab5a1bf283cd81"
SRC_URI[md5sum] = "80a0eac7ab750b01b3f7096a1d4667b8"
SRC_URI[sha256sum] = "754e06dab1c31ab168fc9db9e32596734015ea9e24bc44cae7f237f417ce4efe"

SRC_URI += "\
        "

do_compile() {
        # As of go 1.5, we need a go to build go, so there has to be at least
	# go 1.4 on host -- work out where it is
        hostgo=`which go`
	if [ "x$hostgo" = "x" ]; then
	    bberror "No host golang compiler found."
	fi

	# Get the location of the actual binary (e.g., on debian based systems
	# there is chain of symlinks from /usr/bin/go)
	hostgo_bin=`readlink -f $hostgo`
	hostgo_root="`dirname $hostgo_bin/`/.."

	export GOROOT_BOOTSTRAP=$hostgo_root

	## Setting `$GOBIN` doesn't do any good, looks like it ends up copying
	## binaries there. [ commend from the 1.6.2 meta-virtualization recipe ]
	export GOROOT_FINAL="${SYSROOT}${libdir}/go-1.5"
	export GOHOSTOS="linux"
	export GOOS="linux"

	export GOARCH="${TARGET_ARCH}"
	# golang only support 386, amd64 and arm architecture; translate
	# Yocto arch into go arch
	if [ "${TARGET_ARCH}" = "x86_64" ]; then
		export GOARCH="amd64"
	elif [ "${TARGET_ARCH}" = "i586" ]; then
		export GOARCH="386"
	fi
	if [ "${TARGET_ARCH}" = "arm" ]
	then
		if [ `echo ${TUNE_PKGARCH} | cut -c 1-7` = "cortexa" ]
		then
			echo GOARM 7
			export GOARM="7"
		fi
	fi

        # We want CGO enabled, and we to pass the usual target cflags/ldflags
	# as well as the location of the sysroot through to our gcc, because
	# we cannot pass this via the CC definition.
	export CGO_ENABLED="1"
	export CGO_CFLAGS="${TARGET_CFLAGS} --sysroot=${STAGING_DIR_TARGET}"
	export CGO_LDFLAGS="${TARGET_LDFLAGS} --sysroot=${STAGING_DIR_TARGET}"

        # for the host CC use the BUILD_CC, but take care of any trailing space
	# as this has to be a base file name (I guess the go dist tool does an
	# -x test on this, and it will fail if it is not just a base name; this
	# is a pitty, since we would redally to include the --sysroot here).
        export CC=`${BUILD_CC} | sed -e 's/^[ \t]*$//'`

        # Target compiler vars fortunately allow us to specify the compiler
	# properly
        export CC_FOR_TARGET="${TARGET_PREFIX}gcc ${TARGET_CC_ARCH} --sysroot=${STAGING_DIR_TARGET}"
	export CXX_FOR_TARGET="${TARGET_PREFIX}g++ ${TARGET_CC_ARCH} --sysroot=${STAGING_DIR_TARGET}"
	export GO_GCFLAGS=""
	export GO_LDFLAGS=""

	cd src && ./make.bash
}

do_install() {
	## It should be okay to ignore `${WORKDIR}/go/bin/linux_arm`...
	## Also `gofmt` is not needed right now.
	install -d "${D}${bindir}/go-1.5"
	install -m 0755 "${WORKDIR}/go/bin/go" "${D}${bindir}/go-1.5/"
	install -d "${D}${libdir}/go-1.5"

	## TODO: use `install` instead of `cp`
	for dir in lib pkg src test
	do cp -a "${WORKDIR}/go/${dir}" "${D}${libdir}/go-1.5/"
	done
}
