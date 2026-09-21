package com.example.domain.license

/**
 * Predefined signed license records for Hanouti 40 PRO.
 *
 * Each record represents an authorized production license identified by its
 * cryptographic key-hash (`licenseKeyId`) and signed with the licensing authority's
 * private key. The client verifies records using the public verification key.
 *
 * NOTE: Raw keys and private signing keys are NEVER stored here or in the application binary.
 */
object PredefinedLicenses {

    val CATALOG: List<PredefinedLicenseRecord> = listOf(
        PredefinedLicenseRecord(
            licenseId = "LIC-H40-001",
            licenseKeyId = "cbdd88d035682e0a58f99f05b837d37602b251a9d3b253feb075f566f46d55a1",
            licenseType = "PRO",
            maxDevices = 2,
            createdAt = 1735689600000L,
            expiresAt = Long.MAX_VALUE,
            signature = "MEUCIQD0HHMV1QbDpxVsd9Q/IAMhqqvumO8+RE3kshk4fi4BwwIgKU4A4ANooOko7X8p9Xpz7futDug4iZtC7cgHJWmT0ZM="
        ),
        PredefinedLicenseRecord(
            licenseId = "LIC-H40-002",
            licenseKeyId = "2121bdb8c05f9ca2624ef84e78c9e0f33fd97ba49110c46dc9f97db14b06e585",
            licenseType = "PRO",
            maxDevices = 2,
            createdAt = 1735689600000L,
            expiresAt = Long.MAX_VALUE,
            signature = "MEYCIQD2ddMWh4UAuti+rr86ccrzDUoB9tXeKTMqUpWU6jawegIhAK+RExor/vwFOYPdB4zGl7gVkgqQiWL2diX3+Zks3t4f"
        ),
        PredefinedLicenseRecord(
            licenseId = "LIC-H40-003",
            licenseKeyId = "054cc49e51eb32552d5d5eebbe19f2102f1a0bf47318a0321d0947ed1e329f4f",
            licenseType = "PRO",
            maxDevices = 2,
            createdAt = 1735689600000L,
            expiresAt = Long.MAX_VALUE,
            signature = "MEQCIEkcCa3IVm96XDw/vTGuPyDaKp4wMkCoVaOe1EXdBN8+AiBJMe2+Y3On6SRtxeX/ToD1E6xge4g2DPx2u2ND6oM79g=="
        ),
        PredefinedLicenseRecord(
            licenseId = "LIC-H40-004",
            licenseKeyId = "fc3ece4f19aa04f7d37780d42eed08096c41466b225dd9730d7440c251d5cfe7",
            licenseType = "PRO",
            maxDevices = 2,
            createdAt = 1735689600000L,
            expiresAt = Long.MAX_VALUE,
            signature = "MEUCIQCvwZEtpsp4pguxEEjj4zZsvrcRBb44ibpf0dLJHGcENgIgdXfO54HSoz9o6Z2htwC/1WdUG43RMnZusNaaZ5dT3s8="
        ),
        PredefinedLicenseRecord(
            licenseId = "LIC-H40-005",
            licenseKeyId = "bc0e50086a7bccec325f0c8ba7082c1d0c36022e88387e54aa1dcc39f4c33c67",
            licenseType = "PRO",
            maxDevices = 2,
            createdAt = 1735689600000L,
            expiresAt = Long.MAX_VALUE,
            signature = "MEUCIQC3H3mtJvzmuZVHysx9x8vw4oG3M7wKgv68cxvFVbgE7AIgLXI4wUKaqbzXmLMqK4rPYyug2gtEhdSaSdSToYKsjck="
        ),
        PredefinedLicenseRecord(
            licenseId = "LIC-H40-006",
            licenseKeyId = "fe026b513fccc3026e5812902982dc0854bd56cc6fa26b14dc03b54196e9a381",
            licenseType = "PRO",
            maxDevices = 2,
            createdAt = 1735689600000L,
            expiresAt = Long.MAX_VALUE,
            signature = "MEQCIE32miaxsMRaW1WjOktjftFDEV3mtVZoOX8UOXn9SaNQAiA37heVYNYI4crwqRPF7ivpo5qH+kW+k1DPiuRvE370bg=="
        ),
        PredefinedLicenseRecord(
            licenseId = "LIC-H40-007",
            licenseKeyId = "7f63b9691c4639a590eaa71624b8142180c259e11477e1768c8d0392fe4def25",
            licenseType = "PRO",
            maxDevices = 2,
            createdAt = 1735689600000L,
            expiresAt = Long.MAX_VALUE,
            signature = "MEUCIQDI1d4RPma0l+rZEazeH8IPMd8+m+HZNw5vhQDVAPvbHAIgW89rOEYj0FAyGZNfasUEAz8ZPrc7bW4R+MaCY4gJC38="
        ),
        PredefinedLicenseRecord(
            licenseId = "LIC-H40-008",
            licenseKeyId = "ed5f1fc1cf18435d0978aadbb4eb89221552897d5647d2655a0583fd72fd1285",
            licenseType = "PRO",
            maxDevices = 2,
            createdAt = 1735689600000L,
            expiresAt = Long.MAX_VALUE,
            signature = "MEYCIQCntZ+vUU73mZJNN8UZcR1GynjTtRWc06QONHb6ZL6G5gIhAPP7uaHPTRQc4xemhh5kDnNU2XEBdCn0zcV5ZTE38wFz"
        ),
        PredefinedLicenseRecord(
            licenseId = "LIC-H40-009",
            licenseKeyId = "5c7ab14b832a281a405459bd83a4b35361433d5cf9d8624029bc000362d5ecb8",
            licenseType = "PRO",
            maxDevices = 2,
            createdAt = 1735689600000L,
            expiresAt = Long.MAX_VALUE,
            signature = "MEUCIEqWPVopJNtKmGmR2G/zu68GBLb+AJM1yDdLpl1iRTN6AiEAsVwm9wwz/Yubr2IE+OM9JyAbH3DANWaS5wvew6kmpZE="
        ),
        PredefinedLicenseRecord(
            licenseId = "LIC-H40-010",
            licenseKeyId = "2512ba3ee0e7972caed02e5b017c08c94f81d299b2294a45a6d35d6d8237c1d3",
            licenseType = "PRO",
            maxDevices = 2,
            createdAt = 1735689600000L,
            expiresAt = Long.MAX_VALUE,
            signature = "MEQCIQCq2POnANlQEAdrrEkeORbuKg5dbuTmq1MlTXurQnV0fgIfcVT8EOKQIuF/JL4ZopuHbdan/nshPkf+zXE/JptgAA=="
        ),
        PredefinedLicenseRecord(
            licenseId = "LIC-H40-011",
            licenseKeyId = "6eef7c8255bc19d27d9e135850cde0566052cd6f84ab2d5828f9fce591fcf35e",
            licenseType = "PRO",
            maxDevices = 2,
            createdAt = 1735689600000L,
            expiresAt = Long.MAX_VALUE,
            signature = "MEUCIQC07CN/QBcCbBgob8xv2TJKoSu4VlQeLjKc/DBK5FZOJQIgdN4+0WzQYMNkTax3USiinM6yTZNMXeaaYd24Pw7q91M="
        ),
        PredefinedLicenseRecord(
            licenseId = "LIC-H40-012",
            licenseKeyId = "0f898e9bc43d3bba738bc4df5ce994753be8fff9e5f63409cac3fcfb11e4c8fd",
            licenseType = "PRO",
            maxDevices = 2,
            createdAt = 1735689600000L,
            expiresAt = Long.MAX_VALUE,
            signature = "MEUCIQDD2xz/14Vb0grgz3dV9gKWXZP8esAYEDB4jgQLrh8wOgIgfiCxJRfn7NTE4xuDX6Ebt4DR40UN+PGQdM+bdU9ued8="
        ),
        PredefinedLicenseRecord(
            licenseId = "LIC-H40-013",
            licenseKeyId = "1a12121324416a8992261e5bd3cc264800a04577f622ec9888bbbff481777dd9",
            licenseType = "PRO",
            maxDevices = 2,
            createdAt = 1735689600000L,
            expiresAt = Long.MAX_VALUE,
            signature = "MEQCIEOvLfYQL3DmgeLqry9hQ6/911oEYW83z4+G+oB82ZRXAiAh3JOq9RkDN0yazeFfO6vHNlsALpHFaKeu6UArQvTy4Q=="
        ),
        PredefinedLicenseRecord(
            licenseId = "LIC-H40-014",
            licenseKeyId = "8175a060f57b6d3394d6178eab3322b0f81625948fc7df90d9c1240939197ae4",
            licenseType = "PRO",
            maxDevices = 2,
            createdAt = 1735689600000L,
            expiresAt = Long.MAX_VALUE,
            signature = "MEUCIQCDlhjnNiqD0g+taqjAiWron9Sv9FgZQvlff46ITTeHqQIgWEmEgnteDl1+ehwBv77BtWJYAju0biS/C4ZS0VX4wic="
        ),
        PredefinedLicenseRecord(
            licenseId = "LIC-H40-015",
            licenseKeyId = "05b381584155dd43f3edc16a83807fb53cb4e162b26a6efd8220eeda5a33e702",
            licenseType = "PRO",
            maxDevices = 2,
            createdAt = 1735689600000L,
            expiresAt = Long.MAX_VALUE,
            signature = "MEYCIQDS0NcQOZMA6BX+xSXtnpVqKf7C+jY7O+xbYU0eFXie8QIhAM9EdDoMaeRqSl171LEjGCr1M1Ul8r16ygFRpNEz6Bnb"
        ),
        PredefinedLicenseRecord(
            licenseId = "LIC-H40-016",
            licenseKeyId = "61cdff681038630f3ff56d42b7cab3b39e7c60baf91312e74f30997004cee0ee",
            licenseType = "PRO",
            maxDevices = 2,
            createdAt = 1735689600000L,
            expiresAt = Long.MAX_VALUE,
            signature = "MEUCIQDdPUPG+pOySuM4im1gkrFw3PoVK6NW9GA1wcf1iG/zBwIgIH7/x49FOjN31lkkg/8BPeWh/uctP0cbzbe84rL90/4="
        ),
        PredefinedLicenseRecord(
            licenseId = "LIC-H40-017",
            licenseKeyId = "db6639458e93db3cb1ab1a742f51464cec68fef6993198b5b4b0f4bdc8932983",
            licenseType = "PRO",
            maxDevices = 2,
            createdAt = 1735689600000L,
            expiresAt = Long.MAX_VALUE,
            signature = "MEUCIDr3sv85LiS9D3TTinjF6GPG4Dud7FtsCyxMppzu4H1oAiEAlDDfDf7on6A6dmcavA9Idfy6tnYNb4pQwKswimMaC7M="
        ),
        PredefinedLicenseRecord(
            licenseId = "LIC-H40-018",
            licenseKeyId = "936a044ff5d866d74c198442c84d163456b2013739bbfb7d19bc1b9b99f8db93",
            licenseType = "PRO",
            maxDevices = 2,
            createdAt = 1735689600000L,
            expiresAt = Long.MAX_VALUE,
            signature = "MEQCIHTlpuerP6OQtBgUPgnMh2eTCDNt2aQql6E91YiFPXebAiBAwUfC9zHjbPnSodsVm2C05W0q23Zh+jSsjflOfh34QA=="
        ),
        PredefinedLicenseRecord(
            licenseId = "LIC-H40-019",
            licenseKeyId = "4e595d6bfb3423763471550ec9f1a390faaba7e3054ad66ee27bfcd04bc24304",
            licenseType = "PRO",
            maxDevices = 2,
            createdAt = 1735689600000L,
            expiresAt = Long.MAX_VALUE,
            signature = "MEUCIQD1kU3jcqeJodCfaMrJ6LkeTbaCNZQn6tQQ4BSEvaoWSgIgUR1hNGigSXajMtawKiGPLsUI9hUe8JktTfRcLVE367o="
        ),
        PredefinedLicenseRecord(
            licenseId = "LIC-H40-020",
            licenseKeyId = "c5914f739100bfd839b43e7e27b3bd0cd1fb96ad1d42b87ba455d48c7a5ccbb4",
            licenseType = "PRO",
            maxDevices = 2,
            createdAt = 1735689600000L,
            expiresAt = Long.MAX_VALUE,
            signature = "MEUCIAwR6SZcSPoftwWKadtQoPoN/8QoPQjQOHFvz46cyDE9AiEA7Aqj5N0WVpxM9T8rldU2MtwaGYnJBe1lgdhVltQgiik="
        )
    )
}
