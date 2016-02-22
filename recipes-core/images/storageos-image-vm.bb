SUMMARY="StorageOS VM image"

IMAGE_INSTALL = "packagegroup-core-boot        \
                 ${ROOTFS_PKGMANAGE_BOOTSTRAP} \
                 ${CORE_IMAGE_EXTRA_INSTALL}   \
                 docker                        \
                 libseccomp                    \
                 connman                       \
                 bind-utils                    \
                "

# Add package management, so that the packager (rpm/apt/opkg) can be used
# on the images
IMAGE_FEATURES += "package-management"

# Do we need extra locales?
IMAGE_LINGUAS = " "

# If you want initramfs-based image, then enable this
#INITRD_IMAGE ?= "core-image-minimal-initramfs"

inherit core-image
inherit storageos-image-vm

# the image-vm class supports vmdk, vdi and qcow2 images -- you can generate
# any and all of them by specifying the types here.
IMAGE_FSTYPES = "vmdk"

APPEND =+ "debugshell=5"

# Mabye extra space?
#IMAGE_ROOTFS_EXTRA_SPACE = "41943040"

