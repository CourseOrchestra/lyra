@Library('ratcheting') _
node {
    def server = Artifactory.server 'ART'
    def rtMaven = Artifactory.newMavenBuild()
    def rtNpm = Artifactory.newNpmBuild()
    def buildInfo
    def oldWarnings

    stage ('Clone') {
        checkout scm
    }

    stage ('Artifactory configuration') {
        rtMaven.tool = 'M3'
        rtMaven.deployer releaseRepo: 'libs-release-local', snapshotRepo: 'libs-snapshot-local', server: server
        rtMaven.resolver releaseRepo: 'libs-release', snapshotRepo: 'libs-snapshot', server: server

        rtNpm.tool = 'NODE11'
        rtNpm.deployer repo: 'npm-local', server: server
        rtNpm.resolver repo: 'npm-remote', server: server

        buildInfo = Artifactory.newBuildInfo()
        buildInfo.env.capture = true

        def downloadSpec = """
                 {"files": [
                    {
                      "pattern": "warn/lyra/*/warnings.yml",
                      "build": "lyra :: master/LATEST",
                      "target": "previous.yml",
                      "flat": "true"
                    }
                    ]
                }"""
        server.download spec: downloadSpec
        oldWarnings = readYaml file: 'previous.yml'
    }

    stage ('Spellcheck'){
        result = sh (returnStdout: true,
           script: """for f in \$(find . -name '*.adoc'); do cat \$f | sed "s/-/ /g" | aspell --master=en --personal=./dict list; done | sort | uniq""")
              .trim()
        if (result) {
           echo "The following words are probaly misspelled:"
           echo result
           error "Please correct the spelling or add the words above to the local dictionary."
        }
    }

    stage ('Docker cleanup') {
        sh '''docker ps -a -q &> /dev/null
if [ $? != 0 ]; then
   docker rm $(docker ps -a -q)
fi'''
    }

    try{
        stage ('Exec Maven') {
            rtMaven.run pom: 'pom.xml', goals: 'clean install', buildInfo: buildInfo
        }
    } finally {
        junit 'target/surefire-reports/**/*.xml'
        step( [ $class: 'JacocoPublisher', execPattern: '**/target/jacoco.exec' ] )

        cobertura coberturaReportFile: '**/target/client-coverage/cobertura-coverage.xml'

        def checkstyle = scanForIssues tool: checkStyle(pattern: '**/target/checkstyle-result.xml')
        publishIssues issues: [checkstyle]
        def spotbugs = scanForIssues tool: spotBugs(pattern: '**/target/spotbugsXml.xml')
        publishIssues issues: [spotbugs]
        def eslint = scanForIssues tool: esLint(pattern: '**/target/eslint.xml')
        publishIssues issues: [eslint]
    }

    stage ('Ratcheting') {
        def warningsMap = countWarnings()
        writeYaml file: 'target/warnings.yml', data: warningsMap
        compareWarningMaps oldWarnings, warningsMap
    }

    if (env.BRANCH_NAME == 'master') {
        stage ('NPM publish'){
            rtNpm.publish buildInfo: buildInfo, path: 'src/main/javascript/lyra'
        }

        stage ('Publish build info') {
            def uploadSpec = """
            {
             "files": [
                {
                  "pattern": "target/warnings.yml",
                  "target": "warn/lyra/${currentBuild.number}/warnings.yml"
                }
                ]
            }"""

            def buildInfo2 = server.upload spec: uploadSpec
            buildInfo.append(buildInfo2)
            server.publishBuildInfo buildInfo
        }
    }
}
