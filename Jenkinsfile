pipeline {
	options {
		timeout(time: 40, unit: 'MINUTES')
		buildDiscarder(logRotator(numToKeepStr:'5'))
		disableConcurrentBuilds(abortPrevious: true)
	}
	agent {
		label 'ubuntu-latest'
	}
	tools {
		maven 'apache-maven-latest'
		jdk 'temurin-jdk21-latest'
	}
	stages {
		stage('Build') {
			steps {
				xvnc(useXauthority: true) {
					sh """
						mvn clean verify --update-snapshots --fail-at-end
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
			environment {
				EP_BUILDTOOLS_UPDATES = '/home/data/httpd/download.eclipse.org/eclipse/updates/buildtools'
			}
			steps {
				sshagent ( ['projects-storage.eclipse.org-bot-ssh']) {
					sh '''
						#Determine buildtools version
						mvn help:evaluate -Dexpression=project.version --quiet '-Doutput=projectVersion-value.txt'
						buildtoolsVersion=$(<projectVersion-value.txt)
						rm -f projectVersion-value.txt
						buildtoolsVersion=${buildtoolsVersion%-SNAPSHOT}
						
						epRepoDir="${EP_BUILDTOOLS_UPDATES}/${buildtoolsVersion}"
						ssh genie.platform@projects-storage.eclipse.org rm -rf "${epRepoDir}"
						ssh genie.platform@projects-storage.eclipse.org mkdir -p "${epRepoDir}"
						scp -r repository/target/repository/* genie.platform@projects-storage.eclipse.org:"${epRepoDir}"
						
						# Update the composite to contain this latest deployment
						mvn tycho-p2-repository:modify-composite-repository -Pp2-repository-modification \\
							-Dp2.repository.location=https://download.eclipse.org/eclipse/updates/buildtools/ \\
							-Dp2.repository.output=target/latest-composite \\
							-Dp2.composite.children.add=${buildtoolsVersion} \\
							-Dp2.composite.children.limit=1 \\
							-Dp2.repository.name='Latest Eclipse RelEng Build Tools'
						scp -r target/latest-composite/* genie.platform@projects-storage.eclipse.org:"${EP_BUILDTOOLS_UPDATES}"
					'''
				}
			}
		}
	}
}
