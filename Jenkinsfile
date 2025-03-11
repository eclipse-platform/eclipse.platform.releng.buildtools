pipeline {
	options {
		timeout(time: 40, unit: 'MINUTES')
		buildDiscarder(logRotator(numToKeepStr:'5'))
		disableConcurrentBuilds(abortPrevious: true)
	}
	agent {
		label "ubuntu-latest"
	}
	tools {
		maven 'apache-maven-latest'
		jdk 'temurin-jdk21-latest'
	}
	stages {
		stage('Build') {
			steps {
				wrap([$class: 'Xvnc', useXauthority: true]) {
					sh """
					mvn clean verify --batch-mode --fail-at-end -Dmaven.repo.local=$WORKSPACE/.m2/repository \
						-DskipTests=false -Dcompare-version-with-baselines.skip=false \
						-Dmaven.test.error.ignore=true -Dmaven.test.failure.ignore=true \
						-Dproject.build.sourceEncoding=UTF-8  \
						-Dbuild.sysclasspath=ignore -Dincludeantruntime=false -Dslf4j=false
					"""
				}
			}
			post {
				always {
					archiveArtifacts artifacts: '*.log,*/target/work/data/.metadata/*.log,*/tests/target/work/data/.metadata/*.log,apiAnalyzer-workspace/.metadata/*.log', allowEmptyArchive: true
					junit '**/target/surefire-reports/TEST-*.xml'
					publishIssues issues:[scanForIssues(tool: java()), scanForIssues(tool: mavenConsole())]
				}
			}
		}
		stage('Deploy') {
			when {
				branch 'master'
			}
			steps {
				sshagent ( ['projects-storage.eclipse.org-bot-ssh']) {
					sh 'ssh genie.platform@projects-storage.eclipse.org rm -rf /home/data/httpd/download.eclipse.org/eclipse/updates/buildtools/snapshots'
					sh 'ssh genie.platform@projects-storage.eclipse.org mkdir -p //home/data/httpd/download.eclipse.org/eclipse/updates/buildtools/snapshots'
					sh 'scp -r repository/target/repository/* genie.platform@projects-storage.eclipse.org://home/data/httpd/download.eclipse.org/eclipse/updates/buildtools/snapshots'
				}
			}
		}
	}
}
