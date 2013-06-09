package cucumber.runtime.snippets;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class SnippetGeneratorTest {

    private SnippetGenerator snippetGenerator;

    @Before
    public void setup() {
        snippetGenerator = new SnippetGenerator(null);
    }

    @Test
    public void test_SanitizeFunctionName_using_a_standard_english_sentence() {
        String functionName = " simple step_def  I'd like to write,  as is ";
        String expectedSanitizedFunctionName = "simple_step_def_I_d_like_to_write_as_is";
        assertEquals(expectedSanitizedFunctionName,
                snippetGenerator.sanitizeFunctionName(functionName));
    }

    @Test
    public void test_SanitizeFunctionName_using_several_underscores() {
        String functionName = "_  _one_ word _or two__words_  _ ";
        String expectedSanitizedFunctionName = "__one_word__or_two__words__";
        assertEquals(expectedSanitizedFunctionName,
                snippetGenerator.sanitizeFunctionName(functionName));
    }

    @Test
    public void test_SanitizeFunctionName_using_latin_specific_characters() {
        String functionName = " une étape,  un maître, un hôte  en français sans être exhaustif ";
        String expectedSanitizedFunctionName = "une_etape_un_maitre_un_hote_en_francais_sans_etre_exhaustif";
        assertEquals(expectedSanitizedFunctionName,
                snippetGenerator.sanitizeFunctionName(functionName));
    }

    @Test
    public void test_SanitizeFunctionName_ISO_IEC_8859_1_characters() {
        String functionName = "ÁáÀàÂâÄäÃãÅåÇçÐðÉéÈèÊêËëÍíÌìÎîÏïÑñÓóÒòÔôÖöÕõØøÚúÙùÛûÜüÝýŸÿÆæŒœŠšŽž";
        String expectedSanitizedFunctionName = "AaAaAaAaAaAaCcDdEeEeEeEeIiIiIiIiNnOoOoOoOoOoOoUuUuUuUuYyYyAEaeOEoeShshZhzh";
        assertEquals(expectedSanitizedFunctionName,
                snippetGenerator.sanitizeFunctionName(functionName));
    }

}
