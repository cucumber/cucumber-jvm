repositories.remote << 'http://repo1.maven.org/maven2'

CORE_RUN_ARGS=%w{
cucumber.cli.Main
-f
html
.
}
CORE_MAIN='cucumber.cli.Main'

define 'cucumber-jvm' do
	project.version = '0.0.0'

	define 'core' do
		compile.using(:lint => 'all').with artifacts(:cucumber_html, :diffutils, :gherkin, :gson, :xstream) 
		test.using(:junit).with(artifacts(:mockito, :rhino))

		manifest['Main-Class'] = CORE_MAIN
		package(:jar).merge artifacts(:cucumber_html, :diffutils, :gherkin, :gson, :xstream) 

		run.using(:main => CORE_RUN_ARGS)
	end

end
