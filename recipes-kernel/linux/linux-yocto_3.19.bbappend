FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI += "file://rcu.cfg"
SRC_URI += "file://perf.cfg"
SRC_URI += "file://fs.cfg"
SRC_URI += "file://security.cfg"
SRC_URI += "file://iptables.cfg"

KERNEL_MODULE_AUTOLOAD += "overlay"
