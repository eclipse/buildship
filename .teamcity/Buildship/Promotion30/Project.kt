package Buildship.Promotion30

import Buildship.Promotion30.buildTypes.Milestone
import Buildship.Promotion30.buildTypes.Release
import Buildship.Promotion30.buildTypes.Snapshot
import Buildship.Promotion30.buildTypes.SnapshotAdHoc
import jetbrains.buildServer.configs.kotlin.v2019_2.Project

object Project : Project({
    id("Promotion")
    name = "Promotion"
    description = "Promotes Buildship releases"

    template(PromotionDependencyTemplate)
    template(Promotion30Template)

    buildType(SnapshotAdHoc)
    buildType(Snapshot)
    buildType(Release)
    buildType(Milestone)

    buildTypesOrder = arrayListOf(Release, Milestone, Snapshot, SnapshotAdHoc)
})
