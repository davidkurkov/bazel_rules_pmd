"""
Macros for defining dependencies.
See https://docs.bazel.build/versions/master/skylark/deploying.html#dependencies
"""

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
load("@bazel_tools//tools/build_defs/repo:utils.bzl", "maybe")

def rules_pmd_dependencies():
    """Fetches `rules_pmd` dependencies.

    Declares dependencies of the `rules_pmd` workspace.
    Users should call this macro in their `WORKSPACE` file.
    """

    # Java

    rules_java_version = "5.0.0"
    rules_java_sha = "ddc9e11f4836265fea905d2845ac1d04ebad12a255f791ef7fd648d1d2215a5b"

    maybe(
        repo_rule = http_archive,
        name = "rules_java",
        url = "https://github.com/bazelbuild/rules_java/archive/refs/tags/{v}.tar.gz".format(v = rules_java_version),
        strip_prefix = "rules_java-{v}".format(v = rules_java_version),
        sha256 = rules_java_sha,
    )

    # JVM External

    rules_jvm_external_version = "4.4.2"
    rules_jvm_external_sha = "9004ff5980b3eac3b00041078a7b0abf5d75d30497fbde2c432a838281e22860"

    maybe(
        repo_rule = http_archive,
        name = "rules_jvm_external",
        url = "https://github.com/bazelbuild/rules_jvm_external/archive/{v}.tar.gz".format(v = rules_jvm_external_version),
        strip_prefix = "rules_jvm_external-{v}".format(v = rules_jvm_external_version),
        sha256 = rules_jvm_external_sha,
    )
