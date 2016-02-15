do_install_append() {
    echo "storageos: creating libseccomp.so.2 symlink"
    (cd ${D}/${libdir} && ln -s -f libseccomp.so.0.0.0 libseccomp.so.2)
    rm -rf ${D}/${libdir}/${PN}
}
