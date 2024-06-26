/*
 * Copyright 2024 Aklivity Inc.
 *
 * Aklivity licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.aklivity.k3po.runtime.lang.internal.parser;

import static io.aklivity.k3po.runtime.lang.internal.parser.ScriptParseStrategy.ACCEPT;
import static io.aklivity.k3po.runtime.lang.internal.parser.ScriptParseStrategy.ACCEPTED;
import static io.aklivity.k3po.runtime.lang.internal.parser.ScriptParseStrategy.CLOSE;
import static io.aklivity.k3po.runtime.lang.internal.parser.ScriptParseStrategy.CLOSED;
import static io.aklivity.k3po.runtime.lang.internal.parser.ScriptParseStrategy.CONNECTED;
import static io.aklivity.k3po.runtime.lang.internal.parser.ScriptParseStrategy.CONNECT_ABORT;
import static io.aklivity.k3po.runtime.lang.internal.parser.ScriptParseStrategy.CONNECT_ABORTED;
import static io.aklivity.k3po.runtime.lang.internal.parser.ScriptParseStrategy.EXPRESSION_MATCHER;
import static io.aklivity.k3po.runtime.lang.internal.parser.ScriptParseStrategy.FIXED_LENGTH_BYTES_MATCHER;
import static io.aklivity.k3po.runtime.lang.internal.parser.ScriptParseStrategy.LITERAL_BYTES_VALUE;
import static io.aklivity.k3po.runtime.lang.internal.parser.ScriptParseStrategy.LITERAL_TEXT_VALUE;
import static io.aklivity.k3po.runtime.lang.internal.parser.ScriptParseStrategy.NUMBER_MATCHER;
import static io.aklivity.k3po.runtime.lang.internal.parser.ScriptParseStrategy.PROPERTY_NODE;
import static io.aklivity.k3po.runtime.lang.internal.parser.ScriptParseStrategy.READ;
import static io.aklivity.k3po.runtime.lang.internal.parser.ScriptParseStrategy.READ_ABORT;
import static io.aklivity.k3po.runtime.lang.internal.parser.ScriptParseStrategy.READ_ABORTED;
import static io.aklivity.k3po.runtime.lang.internal.parser.ScriptParseStrategy.READ_ADVISE;
import static io.aklivity.k3po.runtime.lang.internal.parser.ScriptParseStrategy.READ_ADVISED;
import static io.aklivity.k3po.runtime.lang.internal.parser.ScriptParseStrategy.READ_AWAIT;
import static io.aklivity.k3po.runtime.lang.internal.parser.ScriptParseStrategy.READ_CONFIG;
import static io.aklivity.k3po.runtime.lang.internal.parser.ScriptParseStrategy.READ_NOTIFY;
import static io.aklivity.k3po.runtime.lang.internal.parser.ScriptParseStrategy.READ_OPTION;
import static io.aklivity.k3po.runtime.lang.internal.parser.ScriptParseStrategy.REJECTED;
import static io.aklivity.k3po.runtime.lang.internal.parser.ScriptParseStrategy.SCRIPT;
import static io.aklivity.k3po.runtime.lang.internal.parser.ScriptParseStrategy.VARIABLE_LENGTH_BYTES_MATCHER;
import static io.aklivity.k3po.runtime.lang.internal.parser.ScriptParseStrategy.WRITE;
import static io.aklivity.k3po.runtime.lang.internal.parser.ScriptParseStrategy.WRITE_ABORT;
import static io.aklivity.k3po.runtime.lang.internal.parser.ScriptParseStrategy.WRITE_ABORTED;
import static io.aklivity.k3po.runtime.lang.internal.parser.ScriptParseStrategy.WRITE_ADVISE;
import static io.aklivity.k3po.runtime.lang.internal.parser.ScriptParseStrategy.WRITE_ADVISED;
import static io.aklivity.k3po.runtime.lang.internal.parser.ScriptParseStrategy.WRITE_AWAIT;
import static io.aklivity.k3po.runtime.lang.internal.parser.ScriptParseStrategy.WRITE_CONFIG;
import static io.aklivity.k3po.runtime.lang.internal.parser.ScriptParseStrategy.WRITE_NOTIFY;
import static io.aklivity.k3po.runtime.lang.internal.parser.ScriptParseStrategy.WRITE_OPTION;
import static io.aklivity.k3po.runtime.lang.internal.parser.types.DefaultTypeSystem.OPTION_MASK;
import static io.aklivity.k3po.runtime.lang.internal.parser.types.TestTypeSystem.ADVISORY_ADVICE;
import static io.aklivity.k3po.runtime.lang.internal.parser.types.TestTypeSystem.CONFIG_CONFIG;
import static io.aklivity.k3po.runtime.lang.internal.parser.types.TestTypeSystem.OPTION_BYTES;
import static io.aklivity.k3po.runtime.lang.internal.parser.types.TestTypeSystem.OPTION_EXPRESSION;
import static io.aklivity.k3po.runtime.lang.internal.parser.types.TestTypeSystem.OPTION_NUMBER;
import static io.aklivity.k3po.runtime.lang.internal.parser.types.TestTypeSystem.OPTION_STRING;
import static io.aklivity.k3po.runtime.lang.internal.parser.types.TestTypeSystem.OPTION_TRANSPORT;
import static io.aklivity.k3po.runtime.lang.internal.regex.NamedGroupPattern.compile;
import static io.aklivity.k3po.runtime.lang.internal.test.junit.Assert.assertEquals;
import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.util.Arrays;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

import org.junit.Ignore;
import org.junit.Test;

import io.aklivity.k3po.runtime.lang.internal.ast.AstAcceptNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstAcceptedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstCloseNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstClosedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstConnectAbortNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstConnectAbortedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstConnectedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstPropertyNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstReadAbortNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstReadAbortedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstReadAdviseNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstReadAdvisedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstReadAwaitNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstReadConfigNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstReadNotifyNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstReadOptionNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstReadValueNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstRejectedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstScriptNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstWriteAbortNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstWriteAbortedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstWriteAdviseNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstWriteAdvisedNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstWriteAwaitNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstWriteConfigNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstWriteNotifyNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstWriteOptionNode;
import io.aklivity.k3po.runtime.lang.internal.ast.AstWriteValueNode;
import io.aklivity.k3po.runtime.lang.internal.ast.builder.AstAcceptNodeBuilder;
import io.aklivity.k3po.runtime.lang.internal.ast.builder.AstAcceptedNodeBuilder;
import io.aklivity.k3po.runtime.lang.internal.ast.builder.AstCloseNodeBuilder;
import io.aklivity.k3po.runtime.lang.internal.ast.builder.AstClosedNodeBuilder;
import io.aklivity.k3po.runtime.lang.internal.ast.builder.AstConnectAbortNodeBuilder;
import io.aklivity.k3po.runtime.lang.internal.ast.builder.AstConnectAbortedNodeBuilder;
import io.aklivity.k3po.runtime.lang.internal.ast.builder.AstConnectedNodeBuilder;
import io.aklivity.k3po.runtime.lang.internal.ast.builder.AstPropertyNodeBuilder;
import io.aklivity.k3po.runtime.lang.internal.ast.builder.AstReadAbortNodeBuilder;
import io.aklivity.k3po.runtime.lang.internal.ast.builder.AstReadAbortedNodeBuilder;
import io.aklivity.k3po.runtime.lang.internal.ast.builder.AstReadAdviseNodeBuilder;
import io.aklivity.k3po.runtime.lang.internal.ast.builder.AstReadAdvisedNodeBuilder;
import io.aklivity.k3po.runtime.lang.internal.ast.builder.AstReadAwaitNodeBuilder;
import io.aklivity.k3po.runtime.lang.internal.ast.builder.AstReadConfigNodeBuilder;
import io.aklivity.k3po.runtime.lang.internal.ast.builder.AstReadNodeBuilder;
import io.aklivity.k3po.runtime.lang.internal.ast.builder.AstReadNotifyNodeBuilder;
import io.aklivity.k3po.runtime.lang.internal.ast.builder.AstReadOptionNodeBuilder;
import io.aklivity.k3po.runtime.lang.internal.ast.builder.AstRejectedNodeBuilder;
import io.aklivity.k3po.runtime.lang.internal.ast.builder.AstScriptNodeBuilder;
import io.aklivity.k3po.runtime.lang.internal.ast.builder.AstWriteAbortNodeBuilder;
import io.aklivity.k3po.runtime.lang.internal.ast.builder.AstWriteAbortedNodeBuilder;
import io.aklivity.k3po.runtime.lang.internal.ast.builder.AstWriteAdviseNodeBuilder;
import io.aklivity.k3po.runtime.lang.internal.ast.builder.AstWriteAdvisedNodeBuilder;
import io.aklivity.k3po.runtime.lang.internal.ast.builder.AstWriteAwaitNodeBuilder;
import io.aklivity.k3po.runtime.lang.internal.ast.builder.AstWriteConfigNodeBuilder;
import io.aklivity.k3po.runtime.lang.internal.ast.builder.AstWriteNodeBuilder;
import io.aklivity.k3po.runtime.lang.internal.ast.builder.AstWriteNotifyNodeBuilder;
import io.aklivity.k3po.runtime.lang.internal.ast.builder.AstWriteOptionNodeBuilder;
import io.aklivity.k3po.runtime.lang.internal.ast.matcher.AstByteLengthBytesMatcher;
import io.aklivity.k3po.runtime.lang.internal.ast.matcher.AstExactBytesMatcher;
import io.aklivity.k3po.runtime.lang.internal.ast.matcher.AstExactTextMatcher;
import io.aklivity.k3po.runtime.lang.internal.ast.matcher.AstExpressionMatcher;
import io.aklivity.k3po.runtime.lang.internal.ast.matcher.AstFixedLengthBytesMatcher;
import io.aklivity.k3po.runtime.lang.internal.ast.matcher.AstIntLengthBytesMatcher;
import io.aklivity.k3po.runtime.lang.internal.ast.matcher.AstLongLengthBytesMatcher;
import io.aklivity.k3po.runtime.lang.internal.ast.matcher.AstNumberMatcher;
import io.aklivity.k3po.runtime.lang.internal.ast.matcher.AstRegexMatcher;
import io.aklivity.k3po.runtime.lang.internal.ast.matcher.AstShortLengthBytesMatcher;
import io.aklivity.k3po.runtime.lang.internal.ast.matcher.AstValueMatcher;
import io.aklivity.k3po.runtime.lang.internal.ast.matcher.AstVariableLengthBytesMatcher;
import io.aklivity.k3po.runtime.lang.internal.ast.value.AstExpressionValue;
import io.aklivity.k3po.runtime.lang.internal.ast.value.AstLiteralBytesValue;
import io.aklivity.k3po.runtime.lang.internal.ast.value.AstLiteralIntegerValue;
import io.aklivity.k3po.runtime.lang.internal.ast.value.AstLiteralTextValue;
import io.aklivity.k3po.runtime.lang.internal.ast.value.AstLiteralURIValue;
import io.aklivity.k3po.runtime.lang.internal.ast.value.AstValue;
import io.aklivity.k3po.runtime.lang.internal.el.ExpressionContext;
import io.aklivity.k3po.runtime.lang.internal.parser.ScriptParseException;
import io.aklivity.k3po.runtime.lang.internal.parser.ScriptParserImpl;

public class ScriptParserImplTest {

    @Test
    public void shouldParseLiteralText() throws Exception {

        String scriptFragment = "\"012345 test, here!!\"";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstLiteralTextValue actual = parser.parseWithStrategy(scriptFragment, LITERAL_TEXT_VALUE);

        AstLiteralTextValue expected = new AstLiteralTextValue("012345 test, here!!");

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseLiteralTextSingleQuote() throws Exception {

        String scriptFragment = "'012345 test, here!!'";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstLiteralTextValue actual = parser.parseWithStrategy(scriptFragment, LITERAL_TEXT_VALUE);

        AstLiteralTextValue expected = new AstLiteralTextValue("012345 test, here!!");

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseComplexLiteralText() throws Exception {

        String scriptFragment =
                "\"GET / HTTP/1.1\\r\\nHost: localhost:8000\\r\\nUser-Agent: Mozilla/5.0 "
                        + "(Macintosh; Intel Mac OS X 10.6; rv:8.0) Gecko/20100101 Firefox/8.0\\r\\nAccept: text/html,"
                        + "application/xhtml+xml, application/xml;q=0.9,*/*;q=0.8\\r\\n\\r\\n\"";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstLiteralTextValue actual = parser.parseWithStrategy(scriptFragment, LITERAL_TEXT_VALUE);

        AstLiteralTextValue expected =
                new AstLiteralTextValue("GET / HTTP/1.1\r\nHost: localhost:8000\r\nUser-Agent: Mozilla/5.0 "
                        + "(Macintosh; Intel Mac OS X 10.6; rv:8.0) Gecko/20100101 Firefox/8.0\r\nAccept: text/html,"
                        + "application/xhtml+xml, application/xml;q=0.9,*/*;q=0.8\r\n\r\n");

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseComplexLiteralText2() throws Exception {

        String scriptFragment =
                "\"POST /index.html HTTP/1.1\\r\\nHost: localhost:8000\\r\\nUser-Agent: "
                        + "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:8.0) Gecko/20100101 Firefox/8.0\\r\\n"
                        + "Accept: text/html, application/xhtml+xml, application/xml;q=0.9,*/*;q=0.8\\r\\n"
                        + "Content-Length: 43\\r\\n\\r\\nfirst_name=John&last_name=Doe&action=Submit\\r\\n\\r\\n\"";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstLiteralTextValue actual = parser.parseWithStrategy(scriptFragment, LITERAL_TEXT_VALUE);

        AstLiteralTextValue expected =
                new AstLiteralTextValue("POST /index.html HTTP/1.1\r\nHost: "
                        + "localhost:8000\r\nUser-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:8.0) "
                        + "Gecko/20100101 Firefox/8.0\r\nAccept: text/html, application/xhtml+xml, "
                        + "application/xml;q=0.9,*/*;q=0.8\r\nContent-Length: 43\r\n\r\nfirst_name=John"
                        + "&last_name=Doe&action=Submit\r\n\r\n");

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseLiteralBytesValue() throws Exception {

        String scriptFragment = "[0x01 0xff 0XFA]";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstLiteralBytesValue actual = parser.parseWithStrategy(scriptFragment, LITERAL_BYTES_VALUE);

        AstLiteralBytesValue expected = new AstLiteralBytesValue(new byte[]{0x01, (byte) 0xff, (byte) 0xfa});

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseIntLiteral() throws Exception {

        String scriptFragment = "5";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstNumberMatcher actual = parser.parseWithStrategy(scriptFragment, NUMBER_MATCHER);

        AstNumberMatcher expected = new AstNumberMatcher(5);
        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseNegativeIntLiteral() throws Exception {

        String scriptFragment = "-5";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstNumberMatcher actual = parser.parseWithStrategy(scriptFragment, NUMBER_MATCHER);

        AstNumberMatcher expected = new AstNumberMatcher(-5);
        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseHexIntLiteral() throws Exception {

        String scriptFragment = "0x00000005";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstNumberMatcher actual = parser.parseWithStrategy(scriptFragment, NUMBER_MATCHER);

        AstNumberMatcher expected = new AstNumberMatcher(5);
        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseHexIntLiteralWithUnderscores() throws Exception {

        String scriptFragment = "0x0000_0005";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstNumberMatcher actual = parser.parseWithStrategy(scriptFragment, NUMBER_MATCHER);

        AstNumberMatcher expected = new AstNumberMatcher(5);
        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseHexBriefIntLiteral() throws Exception {

        String scriptFragment = "0x5";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstNumberMatcher actual = parser.parseWithStrategy(scriptFragment, NUMBER_MATCHER);

        AstNumberMatcher expected = new AstNumberMatcher(5);
        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseLongLiteral() throws Exception {

        String scriptFragment = "5L";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstNumberMatcher actual = parser.parseWithStrategy(scriptFragment, NUMBER_MATCHER);

        AstNumberMatcher expected = new AstNumberMatcher(5L);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseNegativeLongLiteral() throws Exception {

        String scriptFragment = "-5L";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstNumberMatcher actual = parser.parseWithStrategy(scriptFragment, NUMBER_MATCHER);

        AstNumberMatcher expected = new AstNumberMatcher(-5L);
        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseHexLongLiteral() throws Exception {

        String scriptFragment = "0x0000000000000005L";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstNumberMatcher actual = parser.parseWithStrategy(scriptFragment, NUMBER_MATCHER);

        AstNumberMatcher expected = new AstNumberMatcher(5L);
        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseHexLongLiteralWithUnderscores() throws Exception {

        String scriptFragment = "0x0000_0000_0000_0005L";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstNumberMatcher actual = parser.parseWithStrategy(scriptFragment, NUMBER_MATCHER);

        AstNumberMatcher expected = new AstNumberMatcher(5L);
        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseHexBriefLongLiteral() throws Exception {

        String scriptFragment = "0x5L";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstNumberMatcher actual = parser.parseWithStrategy(scriptFragment, NUMBER_MATCHER);

        AstNumberMatcher expected = new AstNumberMatcher(5L);
        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseFixedLengthBytesMatcher() throws Exception {

        String scriptFragment = "[0..25]";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstFixedLengthBytesMatcher actual = parser.parseWithStrategy(scriptFragment, FIXED_LENGTH_BYTES_MATCHER);

        AstFixedLengthBytesMatcher expected = new AstFixedLengthBytesMatcher(25);
        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseVariableLengthBytesMatcher() throws Exception {

        String scriptFragment = "[0..${len+2}]";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstVariableLengthBytesMatcher actual = parser.parseWithStrategy(scriptFragment, VARIABLE_LENGTH_BYTES_MATCHER);

        ExpressionFactory factory = parser.getExpressionFactory();
        ExpressionContext context = parser.getExpressionContext();
        ValueExpression length = factory.createValueExpression(context, "${len+2}", Integer.class);
        ExpressionContext environment = parser.getExpressionContext();
        AstVariableLengthBytesMatcher expected = new AstVariableLengthBytesMatcher(length, environment);

        assertEquals(expected, actual);
    }

    // @Ignore("not yet supported")
    @Test
    public void shouldParsePrefixedLengthBytesMatcher() throws Exception {

        // String scriptFragment = "[(...){2+}]";
        //
        // ScriptParserImpl parser = new ScriptParserImpl();
        // AstPrefixedLengthBytesMatcher actual =
        // parser.parseWithStrategy(scriptFragment,
        // PREFIXED_LENGTH_BYTES_MATCHER);
        //
        // AstPrefixedLengthBytesMatcher expected = new
        // AstPrefixedLengthBytesMatcher(2);
        //
        // assertEquals(expected, actual);
    }

    @Test
    public void shouldParseExpressionMatcher() throws Exception {

        String scriptFragment = "${ \"\u0001f602\" }";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstExpressionMatcher actual = parser.parseWithStrategy(scriptFragment, EXPRESSION_MATCHER);

        ExpressionFactory factory = parser.getExpressionFactory();
        ExpressionContext context = parser.getExpressionContext();
        ValueExpression value = factory.createValueExpression(context, "${ \"\u0001f602\" }", Object.class);
        AstExpressionMatcher expected = new AstExpressionMatcher(value, parser.getExpressionContext());

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseCapturingFixedLengthBytesMatcher() throws Exception {

        String scriptFragment = "([0..1]:capture)";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstFixedLengthBytesMatcher actual = parser.parseWithStrategy(scriptFragment, FIXED_LENGTH_BYTES_MATCHER);

        AstFixedLengthBytesMatcher expected = new AstFixedLengthBytesMatcher(1, "capture", parser.getExpressionContext());

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseCapturingByteLengthMatcher() throws Exception {

        String scriptFragment = "(byte:capture)";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstFixedLengthBytesMatcher actual = parser.parseWithStrategy(scriptFragment, FIXED_LENGTH_BYTES_MATCHER);

        AstByteLengthBytesMatcher expected = new AstByteLengthBytesMatcher("capture", parser.getExpressionContext());

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseCapturingShortLengthMatcher() throws Exception {

        String scriptFragment = "(short:capture)";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstFixedLengthBytesMatcher actual = parser.parseWithStrategy(scriptFragment, FIXED_LENGTH_BYTES_MATCHER);

        AstShortLengthBytesMatcher expected = new AstShortLengthBytesMatcher("capture", parser.getExpressionContext());

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseCapturingIntLengthMatcher() throws Exception {

        String scriptFragment = "(int:capture)";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstFixedLengthBytesMatcher actual = parser.parseWithStrategy(scriptFragment, FIXED_LENGTH_BYTES_MATCHER);

        AstIntLengthBytesMatcher expected = new AstIntLengthBytesMatcher("capture", parser.getExpressionContext());

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseCapturingLongLengthMatcher() throws Exception {

        String scriptFragment = "(long:capture)";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstFixedLengthBytesMatcher actual = parser.parseWithStrategy(scriptFragment, FIXED_LENGTH_BYTES_MATCHER);

        AstLongLengthBytesMatcher expected = new AstLongLengthBytesMatcher("capture", parser.getExpressionContext());

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseCapturingVariableLengthBytesMatcher() throws Exception {

        String scriptFragment = "([0..${len-45}]:capture)";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstVariableLengthBytesMatcher actual = parser.parseWithStrategy(scriptFragment, VARIABLE_LENGTH_BYTES_MATCHER);

        ExpressionFactory factory = parser.getExpressionFactory();
        ExpressionContext context = parser.getExpressionContext();
        ValueExpression length = factory.createValueExpression(context, "${len-45}", Integer.class);
        AstVariableLengthBytesMatcher expected =
                new AstVariableLengthBytesMatcher(length, "capture", parser.getExpressionContext());

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseExactTextWithQuote() throws Exception {
        String scriptFragment = "\"He\\\"llo\"";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstLiteralTextValue actual = parser.parseWithStrategy(scriptFragment, LITERAL_TEXT_VALUE);

        AstLiteralTextValue expected = new AstLiteralTextValue("He\"llo");

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseEscapedQuoteAndNewline() throws Exception {

        String scriptFragment = "read \"say \\\"hello\\n\\\"\"";
        String expectedValue = "say \"hello\n\"";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadValueNode actual = parser.parseWithStrategy(scriptFragment, READ);

        AstReadValueNode expected = new AstReadValueNode();
        expected.setMatchers(Arrays.<AstValueMatcher>asList(new AstExactTextMatcher(expectedValue)));
        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseEscapedBrackets() throws Exception {
        String scriptFragment = "read \"say [HAHA]\"";
        String expectedValue = "say [HAHA]";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadValueNode actual = parser.parseWithStrategy(scriptFragment, READ);

        AstReadValueNode expected = new AstReadValueNode();
        expected.setMatchers(Arrays.<AstValueMatcher>asList(new AstExactTextMatcher(expectedValue)));
        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseMultiCapturingByteLengthMatcher() throws Exception {

        String scriptFragment = "read (byte:capture) (byte:capture2)";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadValueNode actual = parser.parseWithStrategy(scriptFragment, READ);

        AstReadValueNode expected = new AstReadValueNode();
        expected.setMatchers(Arrays.<AstValueMatcher>asList(
                new AstByteLengthBytesMatcher("capture", parser.getExpressionContext()), new AstByteLengthBytesMatcher(
                        "capture2", parser.getExpressionContext())));
        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseMultiCapturingShortLengthMatcher() throws Exception {

        String scriptFragment = "read (short:capture) (short:capture2)";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadValueNode actual = parser.parseWithStrategy(scriptFragment, READ);

        AstReadValueNode expected = new AstReadValueNode();
        expected.setMatchers(Arrays.<AstValueMatcher>asList(
                new AstShortLengthBytesMatcher("capture", parser.getExpressionContext()), new AstShortLengthBytesMatcher(
                        "capture2", parser.getExpressionContext())));

        assertEquals(expected, actual);
        assertEquals(2, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseMultiCapturingIntLengthMatcher() throws Exception {

        String scriptFragment = "read (int:capture) (int:capture2)";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadValueNode actual = parser.parseWithStrategy(scriptFragment, READ);

        AstReadValueNode expected = new AstReadValueNode();
        expected.setMatchers(Arrays.<AstValueMatcher>asList(
                new AstIntLengthBytesMatcher("capture", parser.getExpressionContext()), new AstIntLengthBytesMatcher("capture2",
                        parser.getExpressionContext())));
        assertEquals(expected, actual);
        assertEquals(2, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseMultiCapturingLongLengthMatcher() throws Exception {

        String scriptFragment = "read (long:capture) (long:capture2)";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadValueNode actual = parser.parseWithStrategy(scriptFragment, READ);

        AstReadValueNode expected = new AstReadValueNode();
        expected.setMatchers(Arrays.<AstValueMatcher>asList(
                new AstLongLengthBytesMatcher("capture", parser.getExpressionContext()), new AstLongLengthBytesMatcher(
                        "capture2", parser.getExpressionContext())));
        assertEquals(expected, actual);
        assertEquals(2, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseMultiExactText() throws Exception {
        String scriptFragment = "read \"Hello\" \"World\"";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadValueNode actual = parser.parseWithStrategy(scriptFragment, READ);

        AstReadValueNode expected = new AstReadValueNode();
        expected.setMatchers(Arrays.<AstValueMatcher>asList(new AstExactTextMatcher("Hello"), new AstExactTextMatcher("World")));
        assertEquals(expected, actual);
        assertEquals(2, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseMultiExactBytes() throws Exception {

        String scriptFragment = "read [0x01 0xff 0XFA] [0x00 0xF0 0x03 0x05 0x08 0x04]";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadValueNode actual = parser.parseWithStrategy(scriptFragment, READ);

        AstReadValueNode expected = new AstReadValueNode();
        expected.setMatchers(Arrays.<AstValueMatcher>asList(new AstExactBytesMatcher(new byte[]{0x01, (byte) 0xff, (byte) 0xfa}), new AstExactBytesMatcher(new byte[]{0x00, (byte) 0xf0, (byte) 0x03, (byte) 0x05,
                (byte) 0x08, (byte) 0x04})));

        assertEquals(expected, actual);
        assertEquals(2, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseMultiExactBytesWithMultipleSpaces() throws Exception {

        String scriptFragment = "read [0x01  0xff    0XFA]  [0x000xF0 0x03 0x05 0x080x04]";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadValueNode actual = parser.parseWithStrategy(scriptFragment, READ);

        AstReadValueNode expected = new AstReadValueNode();
        expected.setMatchers(Arrays.<AstValueMatcher>asList(new AstExactBytesMatcher(new byte[]{0x01, (byte) 0xff, (byte) 0xfa}), new AstExactBytesMatcher(new byte[]{0x00, (byte) 0xf0, (byte) 0x03, (byte) 0x05,
                (byte) 0x08, (byte) 0x04})));

        assertEquals(expected, actual);
        assertEquals(2, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseMultiRegex() throws Exception {
        String scriptFragment = "read /.*\\n/ /.+\\r/";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadValueNode actual = parser.parseWithStrategy(scriptFragment, READ);

        AstReadValueNode expected = new AstReadValueNode();
        ExpressionContext environment = parser.getExpressionContext();
        expected.setMatchers(Arrays.<AstValueMatcher>asList(new AstRegexMatcher(compile(".*\\n"), environment),
                new AstRegexMatcher(compile(".+\\r"), environment)));

        assertEquals(expected, actual);
        assertEquals(2, actual.getRegionInfo().children.size());
        assertEquals(2, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseMultExpression() throws Exception {
        String scriptFragment = "read ${var} ${var2}";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadValueNode actual = parser.parseWithStrategy(scriptFragment, READ);

        ExpressionFactory factory = parser.getExpressionFactory();
        ExpressionContext context = parser.getExpressionContext();
        ValueExpression value = factory.createValueExpression(context, "${var}", Object.class);
        ValueExpression value2 = factory.createValueExpression(context, "${var2}", Object.class);

        AstReadValueNode expected = new AstReadValueNode();
        ExpressionContext environment = parser.getExpressionContext();
        expected.setMatchers(Arrays.<AstValueMatcher>asList(new AstExpressionMatcher(value, environment),
                new AstExpressionMatcher(value2, environment)));

        assertEquals(expected, actual);
        assertEquals(2, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseMultiFixedLengthBytes() throws Exception {
        String scriptFragment = "read [0..1024] [0..4096]";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadValueNode actual = parser.parseWithStrategy(scriptFragment, READ);

        AstReadValueNode expected = new AstReadValueNode();
        expected.setMatchers(Arrays.<AstValueMatcher>asList(new AstFixedLengthBytesMatcher(1024),
                new AstFixedLengthBytesMatcher(4096)));
        assertEquals(expected, actual);
        assertEquals(2, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseMultiFixedLengthBytesWithCaptures() throws Exception {
        String scriptFragment = "read [0..1024] ([0..64]:var1) [0..4096] ([0..64]:var2)";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadValueNode actual = parser.parseWithStrategy(scriptFragment, READ);

        AstReadValueNode expected = new AstReadValueNode();
        ExpressionContext environment = parser.getExpressionContext();
        expected.setMatchers(Arrays.<AstValueMatcher>asList(new AstFixedLengthBytesMatcher(1024),
                new AstFixedLengthBytesMatcher(64, "var1", environment), new AstFixedLengthBytesMatcher(4096),
                new AstFixedLengthBytesMatcher(64, "var2", environment)));

        assertEquals(expected, actual);
        assertEquals(4, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseMultVariableLengthBytes() throws Exception {
        String scriptFragment = "read [0..${len1}] [0..${len2}]";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadValueNode actual = parser.parseWithStrategy(scriptFragment, READ);

        ExpressionFactory factory = parser.getExpressionFactory();
        ExpressionContext context = parser.getExpressionContext();
        ValueExpression value = factory.createValueExpression(context, "${len1}", Integer.class);
        ValueExpression value2 = factory.createValueExpression(context, "${len2}", Integer.class);

        AstReadValueNode expected = new AstReadValueNode();
        ExpressionContext environment = parser.getExpressionContext();
        expected.setMatchers(Arrays.<AstValueMatcher>asList(new AstVariableLengthBytesMatcher(value, environment),
                new AstVariableLengthBytesMatcher(value2, environment)));

        assertEquals(expected, actual);
        assertEquals(2, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseMultVariableLengthBytesWithCapture() throws Exception {
        String scriptFragment = "read ([0..${len1}]:var1) ([0..${len2}]:var2)";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadValueNode actual = parser.parseWithStrategy(scriptFragment, READ);

        ExpressionFactory factory = parser.getExpressionFactory();
        ExpressionContext context = parser.getExpressionContext();
        ValueExpression value = factory.createValueExpression(context, "${len1}", Integer.class);
        ValueExpression value2 = factory.createValueExpression(context, "${len2}", Integer.class);

        AstReadValueNode expected = new AstReadValueNode();
        ExpressionContext environment = parser.getExpressionContext();
        expected.setMatchers(Arrays.<AstValueMatcher>asList(new AstVariableLengthBytesMatcher(value, "var1", environment),
                new AstVariableLengthBytesMatcher(value2, "var2", environment)));

        assertEquals(expected, actual);
        assertEquals(2, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseMultLiteralTextValue() throws Exception {
        String scriptFragment = "write \"Hello\" \"World\"";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstWriteValueNode actual = parser.parseWithStrategy(scriptFragment, WRITE);

        AstWriteValueNode expected = new AstWriteValueNode();
        expected.setValues(Arrays.<AstValue<?>>asList(new AstLiteralTextValue("Hello"), new AstLiteralTextValue("World")));
        assertEquals(expected, actual);
        assertEquals(2, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseMultLiteralBytesValue() throws Exception {
        String scriptFragment = "write [0x01 0x02] [0x03 0x04]";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstWriteValueNode actual = parser.parseWithStrategy(scriptFragment, WRITE);

        AstWriteValueNode expected = new AstWriteValueNode();
        expected.setValues(Arrays.<AstValue<?>>asList(new AstLiteralBytesValue(new byte[]{(byte) 0x01, (byte) 0x02}),
                new AstLiteralBytesValue(new byte[]{(byte) 0x03, (byte) 0x04})));
        assertEquals(expected, actual);
        assertEquals(2, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseMultExpressionValue() throws Exception {
        String scriptFragment = "write ${var1} ${var2}";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstWriteValueNode actual = parser.parseWithStrategy(scriptFragment, WRITE);

        ExpressionFactory factory = parser.getExpressionFactory();
        ExpressionContext context = parser.getExpressionContext();
        ValueExpression value1 = factory.createValueExpression(context, "${var1}", Object.class);
        ValueExpression value2 = factory.createValueExpression(context, "${var2}", Object.class);

        AstWriteValueNode expected = new AstWriteValueNode();
        expected.setValues(Arrays.<AstValue<?>>asList(new AstExpressionValue<>(value1, parser.getExpressionContext()),
                new AstExpressionValue<>(value2, parser.getExpressionContext())));

        assertEquals(expected, actual);
        assertEquals(2, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseMultAllValue() throws Exception {
        String scriptFragment = "write \"Hello\" [0x01 0x02] ${var1}";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstWriteValueNode actual = parser.parseWithStrategy(scriptFragment, WRITE);

        ExpressionFactory factory = parser.getExpressionFactory();
        ExpressionContext context = parser.getExpressionContext();
        ValueExpression value1 = factory.createValueExpression(context, "${var1}", Object.class);

        AstWriteValueNode expected = new AstWriteValueNode();
        expected.setValues(Arrays.<AstValue<?>>asList(new AstLiteralTextValue("Hello"), new AstLiteralBytesValue(new byte[]{
                (byte) 0x01, (byte) 0x02}), new AstExpressionValue<>(value1, parser.getExpressionContext())));

        assertEquals(expected, actual);
        assertEquals(3, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseWriteMultAllValue() throws Exception {
        String scriptFragment = "write \"Hello\" [0x01 0x02] ${var1} 5 5L 0x5 0x5L 0x0000_0005 0x0000_0000_0000_0005L";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstWriteValueNode actual = parser.parseWithStrategy(scriptFragment, WRITE);

        ExpressionFactory factory = parser.getExpressionFactory();
        ExpressionContext context = parser.getExpressionContext();

        AstWriteValueNode expected =
                new AstWriteNodeBuilder()
                        .addExactText("Hello")
                        .addExactBytes(new byte[]{0x01, (byte) 0x02})
                        .addExpression(factory.createValueExpression(context, "${var1}", Object.class),
                                parser.getExpressionContext())
                        .addInteger(5)
                        .addLong(5L)
                        .addInteger(5)
                        .addLong(5L)
                        .addInteger(5)
                        .addLong(5L)
                        .done();

        assertEquals(expected, actual);
        assertEquals(9, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseAccept() throws Exception {

        String scriptFragment = "accept 'http://localhost:8001/echo'";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstAcceptNode actual = parser.parseWithStrategy(scriptFragment, ACCEPT);

        AstAcceptNode expected = new AstAcceptNodeBuilder().setLocation(
                new AstLiteralURIValue(URI.create("http://localhost:8001/echo"))).done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseAcceptWithQueryString() throws Exception {

        String scriptFragment = "accept 'http://localhost:8001/echo?param=value'";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstAcceptNode actual = parser.parseWithStrategy(scriptFragment, ACCEPT);

        AstAcceptNode expected = new AstAcceptNodeBuilder().setLocation(
                new AstLiteralURIValue(URI.create("http://localhost:8001/echo?param=value"))).done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseAcceptWithQueryStringAndPathSegmentParameter() throws Exception {

        String scriptFragment = "accept 'http://localhost:8001/echo/;e/ct?param=value'";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstAcceptNode actual = parser.parseWithStrategy(scriptFragment, ACCEPT);

        AstAcceptNode expected = new AstAcceptNodeBuilder().setLocation(
                new AstLiteralURIValue(URI.create("http://localhost:8001/echo/;e/ct?param=value"))).done();

        assertEquals(expected, actual);
    }

    @Test(expected = ScriptParseException.class)
    public void shouldNotParseAcceptedWithoutBehavior() throws Exception {

        String scriptFragment = "accepted";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstAcceptedNode actual = parser.parseWithStrategy(scriptFragment, ACCEPTED);

        AstAcceptedNode expected = new AstAcceptedNodeBuilder()
                .done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseRejected() throws Exception {

        String scriptFragment = "rejected";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstRejectedNode actual = parser.parseWithStrategy(scriptFragment, REJECTED);

        AstRejectedNode expected = new AstRejectedNodeBuilder()
                .done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseConnectAbort() throws Exception {

        String scriptFragment = "connect abort";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstConnectAbortNode actual = parser.parseWithStrategy(scriptFragment, CONNECT_ABORT);

        AstConnectAbortNode expected = new AstConnectAbortNodeBuilder().done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseConnectAborted() throws Exception {

        String scriptFragment = "connect aborted";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstConnectAbortedNode actual = parser.parseWithStrategy(scriptFragment, CONNECT_ABORTED);

        AstConnectAbortedNode expected = new AstConnectAbortedNodeBuilder().done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseClose() throws Exception {

        String scriptFragment = "close";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstCloseNode actual = parser.parseWithStrategy(scriptFragment, CLOSE);

        AstCloseNode expected = new AstCloseNodeBuilder().done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseWriteAbort() throws Exception {

        String scriptFragment = "write abort";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstWriteAbortNode actual = parser.parseWithStrategy(scriptFragment, WRITE_ABORT);

        AstWriteAbortNode expected = new AstWriteAbortNodeBuilder().done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseReadAbort() throws Exception {

        String scriptFragment = "read abort";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadAbortNode actual = parser.parseWithStrategy(scriptFragment, READ_ABORT);

        AstReadAbortNode expected = new AstReadAbortNodeBuilder().done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseReadAborted() throws Exception {

        String scriptFragment = "read aborted";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadAbortedNode actual = parser.parseWithStrategy(scriptFragment, READ_ABORTED);

        AstReadAbortedNode expected = new AstReadAbortedNodeBuilder().done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseWriteAborted() throws Exception {

        String scriptFragment = "write aborted";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstWriteAbortedNode actual = parser.parseWithStrategy(scriptFragment, WRITE_ABORTED);

        AstWriteAbortedNode expected = new AstWriteAbortedNodeBuilder().done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseClosed() throws Exception {

        String scriptFragment = "closed";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstClosedNode actual = parser.parseWithStrategy(scriptFragment, CLOSED);

        AstClosedNode expected = new AstClosedNodeBuilder().done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseConnected() throws Exception {

        String scriptFragment = "connected";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstConnectedNode actual = parser.parseWithStrategy(scriptFragment, CONNECTED);

        AstConnectedNode expected = new AstConnectedNodeBuilder().done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseReadLiteralText() throws Exception {

        String scriptFragment = "read \"Hello\"";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadValueNode actual = parser.parseWithStrategy(scriptFragment, READ);

        AstReadValueNode expected = new AstReadNodeBuilder().addExactText("Hello").done();

        assertEquals(expected, actual);
        assertEquals(1, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseReadLiteralShort() throws Exception {

        String scriptFragment = "read 5s";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadValueNode actual = parser.parseWithStrategy(scriptFragment, READ);

        AstReadValueNode expected = new AstReadNodeBuilder()
                .addNumber((short)5).done();

        assertEquals(expected, actual);
        assertEquals(1, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseReadLiteralShortWithKeywordOnly() throws Exception {

        String scriptFragment = "read short 5";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadValueNode actual = parser.parseWithStrategy(scriptFragment, READ);

        AstReadValueNode expected = new AstReadNodeBuilder()
                .addNumber((short)5).done();

        assertEquals(expected, actual);
        assertEquals(1, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseReadLiteralShortWithKeywordAndSuffix() throws Exception {

        String scriptFragment = "read short 5s";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadValueNode actual = parser.parseWithStrategy(scriptFragment, READ);

        AstReadValueNode expected = new AstReadNodeBuilder()
                .addNumber((short)5).done();

        assertEquals(expected, actual);
        assertEquals(1, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseReadLiteralInt() throws Exception {

        String scriptFragment = "read 5";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadValueNode actual = parser.parseWithStrategy(scriptFragment, READ);

        AstReadValueNode expected = new AstReadNodeBuilder()
                .addNumber(5).done();

        assertEquals(expected, actual);
        assertEquals(1, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseReadLiteralIntWithKeywordOnly() throws Exception {

        String scriptFragment = "read int 5";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadValueNode actual = parser.parseWithStrategy(scriptFragment, READ);

        AstReadValueNode expected = new AstReadNodeBuilder()
                .addNumber(5).done();

        assertEquals(expected, actual);
        assertEquals(1, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseReadLiteralHexInt() throws Exception {

        String scriptFragment = "read 0x00000005";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadValueNode actual = parser.parseWithStrategy(scriptFragment, READ);

        AstReadValueNode expected = new AstReadNodeBuilder()
                .addNumber(5).done();

        assertEquals(expected, actual);
        assertEquals(1, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseReadLiteralHexIntWithUnderscores() throws Exception {

        String scriptFragment = "read 0x0000_0005";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadValueNode actual = parser.parseWithStrategy(scriptFragment, READ);

        AstReadValueNode expected = new AstReadNodeBuilder()
                .addNumber(5).done();

        assertEquals(expected, actual);
        assertEquals(1, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseReadLiteralLong() throws Exception {

        String scriptFragment = "read 5L";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadValueNode actual = parser.parseWithStrategy(scriptFragment, READ);

        AstReadValueNode expected = new AstReadNodeBuilder()
                .addNumber(5L).done();

        assertEquals(expected, actual);
        assertEquals(1, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseReadLiteralLongWithKeywordOnly() throws Exception {

        String scriptFragment = "read long 5";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadValueNode actual = parser.parseWithStrategy(scriptFragment, READ);

        AstReadValueNode expected = new AstReadNodeBuilder()
                .addNumber(5L).done();

        assertEquals(expected, actual);
        assertEquals(1, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseReadLiteralLongWithKeywordAndSuffix() throws Exception {

        String scriptFragment = "read long 5L";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadValueNode actual = parser.parseWithStrategy(scriptFragment, READ);

        AstReadValueNode expected = new AstReadNodeBuilder()
                .addNumber(5L).done();

        assertEquals(expected, actual);
        assertEquals(1, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseReadLiteralHexBriefLong() throws Exception {

        String scriptFragment = "read 0x5L";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadValueNode actual = parser.parseWithStrategy(scriptFragment, READ);

        AstReadValueNode expected = new AstReadNodeBuilder()
                .addNumber(5L).done();

        assertEquals(expected, actual);
        assertEquals(1, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseReadLiteralHexLong() throws Exception {

        String scriptFragment = "read 0x0000000000000005L";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadValueNode actual = parser.parseWithStrategy(scriptFragment, READ);

        AstReadValueNode expected = new AstReadNodeBuilder()
                .addNumber(5L).done();

        assertEquals(expected, actual);
        assertEquals(1, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseReadLiteralHexLongWithUnderscores() throws Exception {

        String scriptFragment = "read 0x0000_0000_0000_0005L";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadValueNode actual = parser.parseWithStrategy(scriptFragment, READ);

        AstReadValueNode expected = new AstReadNodeBuilder()
                .addNumber(5L).done();

        assertEquals(expected, actual);
        assertEquals(1, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseReadLiteralTextWithMuchPunctuation() throws Exception {

        String scriptFragment =
                "read \"HTTP/1.1 404 Not Found\\r\\nServer: Web Server\\r\\n"
                        + "Date: Thu, 03 May 2012 20:41:24 GMT\\r\\n\\r\\nContent-Type: text/html\\r\\n"
                        + "Content-length: 61 <html><head></head><body><h1>404 Not Found</h1></body></html>\"";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadValueNode actual = parser.parseWithStrategy(scriptFragment, READ);

        AstReadValueNode expected =
                new AstReadNodeBuilder()

                .addExactText(
                        "HTTP/1.1 404 Not Found\r\nServer: Web Server\r\n"
                                + "Date: Thu, 03 May 2012 20:41:24 GMT\r\n\r\nContent-Type: text/html\r\n"
                                + "Content-length: 61 <html><head></head><body><h1>404 Not Found</h1></body></html>").done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseReadLiteralBytes() throws Exception {

        String scriptFragment = "read [0x01 0x02 0xFF]";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadValueNode actual = parser.parseWithStrategy(scriptFragment, READ);

        AstReadValueNode expected = new AstReadNodeBuilder()

        .addExactBytes(new byte[]{0x01, 0x02, (byte) 0xff}, parser.getExpressionContext()).done();

        assertEquals(expected, actual);
        assertEquals(1, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseReadExpression() throws Exception {

        String scriptFragment = "read ${hello}";

        ScriptParserImpl parser = new ScriptParserImpl();
        ExpressionFactory factory = parser.getExpressionFactory();
        ExpressionContext context = parser.getExpressionContext();

        AstReadValueNode actual = parser.parseWithStrategy(scriptFragment, READ);

        AstReadValueNode expected = new AstReadNodeBuilder()

        .addExpression(factory.createValueExpression(context, "${hello}", Object.class), parser.getExpressionContext()).done();

        assertEquals(expected, actual);
        assertEquals(1, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseWriteLiteralTextWithSlash() throws Exception {

        String scriptFragment = "write \"GET /index.html blah\"";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstWriteValueNode actual = parser.parseWithStrategy(scriptFragment, WRITE);

        AstWriteValueNode expected = new AstWriteNodeBuilder().addExactText("GET /index.html blah").done();

        assertEquals(expected, actual);
        assertEquals(1, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseWriteLiteralShort() throws Exception {

        String scriptFragment = "write 5s";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstWriteValueNode actual = parser.parseWithStrategy(scriptFragment, WRITE);

        AstWriteValueNode expected = new AstWriteNodeBuilder()
                .addShort((short)5).done();

        assertEquals(expected, actual);
        assertEquals(1, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseWriteLiteralShortWithKeywordOnly() throws Exception {

        String scriptFragment = "write short 5";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstWriteValueNode actual = parser.parseWithStrategy(scriptFragment, WRITE);

        AstWriteValueNode expected = new AstWriteNodeBuilder()
                .addShort((short)5).done();

        assertEquals(expected, actual);
        assertEquals(1, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseWriteLiteralShortWithKeywordAndSuffix() throws Exception {

        String scriptFragment = "write short 5s";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstWriteValueNode actual = parser.parseWithStrategy(scriptFragment, WRITE);

        AstWriteValueNode expected = new AstWriteNodeBuilder()
                .addShort((short)5).done();

        assertEquals(expected, actual);
        assertEquals(1, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseWriteLiteralInt() throws Exception {

        String scriptFragment = "write 5";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstWriteValueNode actual = parser.parseWithStrategy(scriptFragment, WRITE);

        AstWriteValueNode expected = new AstWriteNodeBuilder()
                .addInteger(5).done();

        assertEquals(expected, actual);
        assertEquals(1, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseWriteLiteralIntWithKeywordOnly() throws Exception {

        String scriptFragment = "write int 5";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstWriteValueNode actual = parser.parseWithStrategy(scriptFragment, WRITE);

        AstWriteValueNode expected = new AstWriteNodeBuilder()
                .addInteger(5).done();

        assertEquals(expected, actual);
        assertEquals(1, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseWriteLiteralLong() throws Exception {

        String scriptFragment = "write 5L";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstWriteValueNode actual = parser.parseWithStrategy(scriptFragment, WRITE);

        AstWriteValueNode expected = new AstWriteNodeBuilder()
                .addLong(5L).done();

        assertEquals(expected, actual);
        assertEquals(1, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseWriteLiteralLongWithKeywordOnly() throws Exception {

        String scriptFragment = "write long 5";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstWriteValueNode actual = parser.parseWithStrategy(scriptFragment, WRITE);

        AstWriteValueNode expected = new AstWriteNodeBuilder()
                .addLong(5L).done();

        assertEquals(expected, actual);
        assertEquals(1, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseWriteLiteralLongWithKeywordAndSuffix() throws Exception {

        String scriptFragment = "write long 5L";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstWriteValueNode actual = parser.parseWithStrategy(scriptFragment, WRITE);

        AstWriteValueNode expected = new AstWriteNodeBuilder()
                .addLong(5L).done();

        assertEquals(expected, actual);
        assertEquals(1, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseWriteLiteralTextWithAsterisk() throws Exception {

        String scriptFragment = "write \"GET /index.html blah*\"";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstWriteValueNode actual = parser.parseWithStrategy(scriptFragment, WRITE);

        AstWriteValueNode expected = new AstWriteNodeBuilder().addExactText("GET /index.html blah*").done();

        assertEquals(expected, actual);
        assertEquals(1, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseWriteLiteralTextWithDollarSign() throws Exception {

        String scriptFragment = "write \"GET $foo\"";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstWriteValueNode actual = parser.parseWithStrategy(scriptFragment, WRITE);

        AstWriteValueNode expected = new AstWriteNodeBuilder().addExactText("GET $foo").done();

        assertEquals(expected, actual);
        assertEquals(1, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseWriteLiteralTextWithMuchPunctuation() throws Exception {

        String scriptFragment =
                "write \"GET / HTTP/1.1\\r\\nHost: localhost:8000\\r\\n"
                        + "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:8.0) Gecko/20100101 Firefox/8.0\\r\\n"
                        + "Accept: text/html, application/xhtml+xml, application/xml;q=0.9,*/*;q=0.8\\r\\n\\r\\n\"";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstWriteValueNode actual = parser.parseWithStrategy(scriptFragment, WRITE);

        AstWriteValueNode expected =
                new AstWriteNodeBuilder().addExactText(
                        "GET / HTTP/1.1\r\nHost: localhost:8000\r\n"
                                + "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:8.0) "
                                + "Gecko/20100101 Firefox/8.0\r\n"
                                + "Accept: text/html, application/xhtml+xml, application/xml;q=0.9,*/*;q=0.8\r\n\r\n").done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseWriteLiteralTextWithSingleQuote() throws Exception {

        String scriptFragment = "write \"DON'T WORK\"";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstWriteValueNode actual = parser.parseWithStrategy(scriptFragment, WRITE);

        AstWriteValueNode expected = new AstWriteNodeBuilder().addExactText("DON'T WORK").done();

        assertEquals(expected, actual);
        assertEquals(1, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseWriteLongLiteralText() throws Exception {

        StringBuilder longLiteralTextBuilder = new StringBuilder();
        longLiteralTextBuilder.append("POST /index.html HTTP/1.1\\r\\nHost: localhost:8000\\r\\n"
                + "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:8.0) Gecko/20100101 Firefox/8.0\\r\\n"
                + "Accept: text/html, application/xhtml+xml, application/xml;q=0.9,*/*;q=0.8\\r\\n"
                + "Content-Length: 99860\\r\\n\\r\\nfirst_name=Johnlast_nameDoeactionSubmitLoremipsumdolorsitametconsectetur");
        for (int i = 0; i < 3030; i++) {
            longLiteralTextBuilder.append("Loremipsumdolorsitametconsectetur");
        }
        String longLiteralText = longLiteralTextBuilder.toString();

        String scriptFragment = String.format("write \"%s\"", longLiteralText);

        ScriptParserImpl parser = new ScriptParserImpl();
        AstWriteValueNode actual = parser.parseWithStrategy(scriptFragment, WRITE);

        longLiteralTextBuilder = new StringBuilder();
        longLiteralTextBuilder.append("POST /index.html HTTP/1.1\r\nHost: localhost:8000\r\n"
                + "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:8.0) Gecko/20100101 Firefox/8.0\r\n"
                + "Accept: text/html, application/xhtml+xml, application/xml;q=0.9,*/*;q=0.8\r\n"
                + "Content-Length: 99860\r\n\r\nfirst_name=Johnlast_nameDoeactionSubmitLoremipsumdolorsitametconsectetur");
        for (int i = 0; i < 3030; i++) {
            longLiteralTextBuilder.append("Loremipsumdolorsitametconsectetur");
        }
        longLiteralText = longLiteralTextBuilder.toString();

        AstWriteValueNode expected = new AstWriteNodeBuilder().addExactText(longLiteralText).done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseReadAwaitBarrier() throws Exception {

        String scriptFragment = "read await BARRIER";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadAwaitNode actual = parser.parseWithStrategy(scriptFragment, READ_AWAIT);

        AstReadAwaitNode expected = new AstReadAwaitNodeBuilder().setBarrierName("BARRIER").done();

        assertEquals(expected, actual);
        assertEquals(0, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseReadNotifyBarrier() throws Exception {

        String scriptFragment = "read notify BARRIER";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadNotifyNode actual = parser.parseWithStrategy(scriptFragment, READ_NOTIFY);

        AstReadNotifyNode expected = new AstReadNotifyNodeBuilder().setBarrierName("BARRIER").done();

        assertEquals(expected, actual);
        assertEquals(0, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseWriteAwaitBarrier() throws Exception {

        String scriptFragment = "write await BARRIER";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstWriteAwaitNode actual = parser.parseWithStrategy(scriptFragment, WRITE_AWAIT);

        AstWriteAwaitNode expected = new AstWriteAwaitNodeBuilder().setBarrierName("BARRIER").done();

        assertEquals(expected, actual);
        assertEquals(0, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseWriteNotifyBarrier() throws Exception {

        String scriptFragment = "write notify BARRIER";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstWriteNotifyNode actual = parser.parseWithStrategy(scriptFragment, WRITE_NOTIFY);

        AstWriteNotifyNode expected = new AstWriteNotifyNodeBuilder().setBarrierName("BARRIER").done();

        assertEquals(expected, actual);
        assertEquals(0, actual.getRegionInfo().children.size());
    }

    @Test
    public void shouldParseConnectScript() throws Exception {

        String script =
                "# tcp.client.connect-then-close\n" +
                "connect 'http://localhost:8080/path?p1=v1&p2=v2'\n" +
                "connected\n" +
                "close\n" +
                "closed\n";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstScriptNode actual = parser.parseWithStrategy(script, SCRIPT);
        AstValue<URI> location = new AstLiteralURIValue(URI.create("http://localhost:8080/path?p1=v1&p2=v2"));
        AstScriptNode expected =
                new AstScriptNodeBuilder().addConnectStream().setLocation(location)
                        .addConnectedEvent().done().addCloseCommand().done().addClosedEvent().done().done().done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseConnectWhenScript() throws Exception {

        // @formatter:off
        String script =
                "# tcp.client.connect-then-close\n" +
                "connect await BARRIER\r\n" +
                "        'http://localhost:8080/path?p1=v1&p2=v2'\n" +
                "connected\n" +
                "close\n" +
                "closed\n";
        // @formatter:on

        ScriptParserImpl parser = new ScriptParserImpl();
        AstScriptNode actual = parser.parseWithStrategy(script, SCRIPT);
        AstValue<URI> location = new AstLiteralURIValue(URI.create("http://localhost:8080/path?p1=v1&p2=v2"));

        // @formatter:off
         AstScriptNode expected = new AstScriptNodeBuilder()
                 .addConnectStream()
                     .setLocation(location)
                     .setAwaitName("BARRIER")
                     .addConnectedEvent()
                     .done()
                     .addCloseCommand()
                     .done()
                     .addClosedEvent()
                     .done()
                 .done()
             .done();

        assertEquals(expected, actual);
        // @formatter:on
    }

    @Test
    public void shouldParseConnectScriptWithComments() throws Exception {

        String script =
                "# tcp.client.connect-then-close\n" +
                "connect 'tcp://localhost:7788' # Comment 1\n" +
                "\t\t # Comment 2\n" +
                "connected\n" +
                "close\n" +
                "closed\n";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstScriptNode actual = parser.parseWithStrategy(script, SCRIPT);
        AstValue<URI> location = new AstLiteralURIValue(URI.create("tcp://localhost:7788"));

        AstScriptNode expected =
                new AstScriptNodeBuilder().addConnectStream().setLocation(location)
                        .addConnectedEvent().done().addCloseCommand().done().addClosedEvent().done().done().done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseConnectScriptWithOptions() throws Exception {

        String script =
                "connect 'test://authority'\n" +
                "       option test:transport 'tcp://localhost:8000'\n" +
                "       option test:string \"text\"" +
                "       option test:bytes [0x01 0x02 0x03 0x04]" +
                "       option test:number 1234" +
                "       option test:expression ${variable}" +
                "connected\n" +
                "close\n" +
                "closed\n";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstScriptNode actual = parser.parseWithStrategy(script, SCRIPT);
        AstValue<URI> location = new AstLiteralURIValue(URI.create("test://authority"));
        AstValue<URI> transport = new AstLiteralURIValue(URI.create("tcp://localhost:8000"));
        AstValue<String> string = new AstLiteralTextValue("text");
        AstValue<byte[]> bytes = new AstLiteralBytesValue(new byte[] { 0x01, 0x02, 0x03, 0x04 });
        AstValue<Integer> number = new AstLiteralIntegerValue(1234);

        ExpressionFactory factory = parser.getExpressionFactory();
        ExpressionContext environment = parser.getExpressionContext();
        ValueExpression valueExpression = factory.createValueExpression(environment, "${variable}", Object.class);
        AstExpressionValue<Object> expression = new AstExpressionValue<>(valueExpression, environment);

        AstScriptNode expected = new AstScriptNodeBuilder()
                .addConnectStream()
                    .setLocation(location)
                    .setOption(OPTION_TRANSPORT, transport)
                    .setOption(OPTION_STRING, string)
                    .setOption(OPTION_BYTES, bytes)
                    .setOption(OPTION_NUMBER, number)
                    .setOption(OPTION_EXPRESSION, expression)
                    .addConnectedEvent().done()
                    .addCloseCommand().done()
                    .addClosedEvent().done()
                .done()
        .done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseAcceptScript() throws Exception {

        String script =
                "# tcp.client.accept-then-close\n" +
                "accept 'tcp://localhost:7788'\n" +
                "accepted\n" +
                "connected\n" +
                "close\n" +
                "closed\n";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstScriptNode actual = parser.parseWithStrategy(script, SCRIPT);

        AstScriptNode expected = new AstScriptNodeBuilder().addAcceptStream()
                .setLocation(new AstLiteralURIValue(URI.create("tcp://localhost:7788"))).done().addAcceptedStream()
                .addConnectedEvent().done().addCloseCommand().done().addClosedEvent().done().done().done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseAcceptScriptWithOptions() throws Exception {

        String script =
                "accept 'test://authority'\n" +
                "       option test:transport 'tcp://localhost:8000'\n" +
                "       option test:string \"text\"" +
                "       option test:bytes [0x01 0x02 0x03 0x04]" +
                "       option test:number 1234" +
                "       option test:expression ${variable}" +
                "accepted\n" +
                "connected\n" +
                "close\n" +
                "closed\n";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstScriptNode actual = parser.parseWithStrategy(script, SCRIPT);

        AstValue<URI> location = new AstLiteralURIValue(URI.create("test://authority"));
        AstValue<URI> transport = new AstLiteralURIValue(URI.create("tcp://localhost:8000"));
        AstValue<String> string = new AstLiteralTextValue("text");
        AstValue<byte[]> bytes = new AstLiteralBytesValue(new byte[] { 0x01, 0x02, 0x03, 0x04 });
        AstValue<Integer> number = new AstLiteralIntegerValue(1234);

        ExpressionFactory factory = parser.getExpressionFactory();
        ExpressionContext environment = parser.getExpressionContext();
        ValueExpression valueExpression = factory.createValueExpression(environment, "${variable}", Object.class);
        AstExpressionValue<Object> expression = new AstExpressionValue<>(valueExpression, environment);

        AstScriptNode expected = new AstScriptNodeBuilder()
                .addAcceptStream()
                    .setLocation(location)
                    .setOption(OPTION_TRANSPORT, transport)
                    .setOption(OPTION_STRING, string)
                    .setOption(OPTION_BYTES, bytes)
                    .setOption(OPTION_NUMBER, number)
                    .setOption(OPTION_EXPRESSION, expression)
                .done()
                .addAcceptedStream()
                    .addConnectedEvent().done()
                    .addCloseCommand().done()
                    .addClosedEvent().done()
                .done()
        .done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseMultiConnectScript() throws Exception {

        String script =
                "# tcp.client.echo-multi-conn.upstream\n" +
                "connect 'tcp://localhost:8785'\n" +
                "connected\n" +
                "write \"Hello, world!\"\n" +
                "write notify BARRIER\n" +
                "close\n" +
                "closed\n" +
                "# tcp.client.echo-multi-conn.downstream\n" +
                "connect 'tcp://localhost:8783'\n" +
                "connected\n" +
                "read await BARRIER\n" +
                "read \"Hello, world!\"\n" +
                "close\n" +
                "closed\n";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstScriptNode actual = parser.parseWithStrategy(script, SCRIPT);
        AstValue<URI> location8785 = new AstLiteralURIValue(URI.create("tcp://localhost:8785"));
        AstValue<URI> location8783 = new AstLiteralURIValue(URI.create("tcp://localhost:8783"));

        AstScriptNode expected =
                new AstScriptNodeBuilder().addConnectStream().setLocation(location8785)
                        .addConnectedEvent().done().addWriteCommand().addExactText("Hello, world!").done()
                        .addWriteNotifyBarrier().setBarrierName("BARRIER").done().addCloseCommand().done().addClosedEvent()
                        .done().done().addConnectStream().setLocation(location8783).addConnectedEvent()
                        .done().addReadAwaitBarrier().setBarrierName("BARRIER").done().addReadEvent()
                        .addExactText("Hello, world!").done().addCloseCommand().done().addClosedEvent().done().done().done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseMultiAcceptScript() throws Exception {

        String script =
                "# tcp.server.echo-multi-conn.upstream\n" +
                "accept 'tcp://localhost:8783'\n" +
                "accepted\n" +
                "connected\n" +
                "read await BARRIER\n" +
                "read \"Hello, world!\"\n" +
                "close\n" +
                "closed\n" +
                "# tcp.server.echo-multi-conn.downstream\n" +
                "accept 'tcp://localhost:8785'\n" +
                "accepted\n" +
                "connected\n" +
                "write \"Hello, world!\"\n" +
                "write notify BARRIER\n" +
                "close\n" +
                "closed\n";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstScriptNode actual = parser.parseWithStrategy(script, SCRIPT);

        AstScriptNode expected = new AstScriptNodeBuilder().addAcceptStream()
                .setLocation(new AstLiteralURIValue(URI.create("tcp://localhost:8783"))).done().addAcceptedStream()
                .addConnectedEvent().done().addReadAwaitBarrier().setBarrierName("BARRIER").done().addReadEvent()
                .addExactText("Hello, world!").done().addCloseCommand().done().addClosedEvent().done().done()
                .addAcceptStream().setLocation(new AstLiteralURIValue(URI.create("tcp://localhost:8785"))).done()
                .addAcceptedStream().addConnectedEvent().done().addWriteCommand().addExactText("Hello, world!").done()
                .addWriteNotifyBarrier().setBarrierName("BARRIER").done().addCloseCommand().done().addClosedEvent()
                .done().done().done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseAcceptAndConnectScript() throws Exception {

        String script =
                "# tcp.server.accept-then-close\n" +
                "accept 'tcp://localhost:7788'\n" +
                "accepted\n" +
                "connected\n" +
                "closed\n" +
                "# tcp.client.connect-then-close\n" +
                "connect 'tcp://localhost:7788'\n" +
                "connected\n" +
                "close\n" +
                "closed\n";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstValue<URI> location7788 = new AstLiteralURIValue(URI.create("tcp://localhost:7788"));
        AstScriptNode actual = parser.parseWithStrategy(script, SCRIPT);
        AstScriptNode expected = new AstScriptNodeBuilder().addAcceptStream()
                .setLocation(new AstLiteralURIValue(URI.create("tcp://localhost:7788"))).done().addAcceptedStream()
                .addConnectedEvent().done().addClosedEvent().done().done().addConnectStream().setLocation(location7788)
                .addConnectedEvent().done().addCloseCommand().done().addClosedEvent().done().done().done();
        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseNonClosingConnectScript() throws Exception {

        String script =
                "# tcp.client.non-closing\n" +
                "connect 'tcp://localhost:7788'\n" +
                "connected\n" +
                "read \"foo\"\n" +
                "write [0x01 0x02 0xff]\n" +
                "closed\n";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstScriptNode actual = parser.parseWithStrategy(script, SCRIPT);
        AstValue<URI> location7788 = new AstLiteralURIValue(URI.create("tcp://localhost:7788"));
        AstScriptNode expected =
                new AstScriptNodeBuilder().addConnectStream().setLocation(location7788)
                        .addConnectedEvent().done().addReadEvent().addExactText("foo").done().addWriteCommand()
                        .addExactBytes(new byte[]{0x01, 0x02, (byte) 0xff}).done().addClosedEvent().done().done().done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseEmptyScript() throws Exception {

        String script = "";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstScriptNode actual = parser.parseWithStrategy(script, SCRIPT);

        AstScriptNode expected = new AstScriptNodeBuilder().done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseScriptWithCommentsOnly() throws Exception {

        String script = "# Comment 1\n" + "# Comment 2\n" + "# Comment 3\n";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstScriptNode actual = parser.parseWithStrategy(script, SCRIPT);

        AstScriptNode expected = new AstScriptNode();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseScriptWithCommentsAndWhitespace() throws Exception {

        String script = "# Comment 1\n" + "\t\n" + " # Comment 2\n" + "\r\n" + "# Comment 3\n";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstScriptNode actual = parser.parseWithStrategy(script, SCRIPT);

        AstScriptNode expected = new AstScriptNode();

        assertEquals(expected, actual);
    }

    @Ignore("Not implemented and perhaps not designed correctly.  "
            + "Need to access the use and proper syntax for child channels")
    @Test
    public void shouldParseScript() throws Exception {

        String script =
                "#\n" + "# server\n" + "#\n" + "accept tcp://localhost:8000 as ACCEPT\n" + "opened\n" + "bound\n"
                        + "child opened\n" + "child closed\n" + "unbound\n" + "closed\n" + "#\n" + "# child\n" + "#\n"
                        + " accepted ACCEPT\n" + "opened\n" + " bound\n" + "connected\n" + " read ([0..32]:input)\n"
                        + "read notify BARRIER\n" + "write await BARRIER\n" + "write [ 0x01 0xfe ]\n" + "close\n"
                        + "disconnected\n" + "unbound\n" + "closed\n" + "#\n" + "# client\n" + "#\n"
                        + "connect tcp://localhost:8000\n" + " opened\n" + "bound\n" + " connected\n" + "write ${input}\n"
                        + " read [ 0x00 0xff ]\n" + "close\n" + "disconnected\n" + "unbound\n" + "closed";

        ExpressionFactory factory = ExpressionFactory.newInstance();
        ExpressionContext context = new ExpressionContext();

        ScriptParserImpl parser = new ScriptParserImpl(factory, context);
        // parser.lex(new ByteArrayInputStream(script.getBytes(UTF_8)));
        AstScriptNode actual = parser.parseWithStrategy(script, SCRIPT);
        AstValue<URI> location8000 = new AstLiteralURIValue(URI.create("tcp://localhost:8000"));

        AstScriptNode expected = new AstScriptNodeBuilder().addAcceptStream()
                .setLocation(new AstLiteralURIValue(URI.create("tcp://localhost:8000"))).setAcceptName("ACCEPT")
                .addOpenedEvent()
                .done().addBoundEvent()
                .done().addChildOpenedEvent()
                .done().addChildClosedEvent()
                .done().addUnboundEvent()
                .done().addClosedEvent()
                .done().done().addAcceptedStream().setAcceptName("ACCEPT").addOpenedEvent()
                .done().addBoundEvent().done().addConnectedEvent()
                .done().addReadEvent().addFixedLengthBytes(32, "input", parser.getExpressionContext()).done()
                .addReadNotifyBarrier()
                .setBarrierName("BARRIER").done().addWriteAwaitBarrier()
                .setBarrierName("BARRIER").done().addWriteCommand()
                .addExactBytes(new byte[] { 0x01, -0x02 }).done().addCloseCommand()
                .done().addDisconnectedEvent()
                .done().addUnboundEvent()
                .done().addClosedEvent()
                .done().done().addConnectStream().setLocation(location8000).addOpenedEvent().done().addBoundEvent()
                .done().addConnectedEvent().done().addWriteCommand()
                .addExpression(factory.createValueExpression(context, "${input}", Object.class), context).done()
                .addReadEvent().addExactBytes(new byte[] { 0x00, -0x01 }, context).done().addCloseCommand().done()
                .addDisconnectedEvent().done().addUnboundEvent().done().addClosedEvent().done().done().done();

        assertEquals(expected, actual);
    }

    @Test(
        expected = ScriptParseException.class)
    public void shouldNotParseScriptWithUnknownKeyword() throws Exception {

        String script = "written\n";

        ScriptParserImpl parser = new ScriptParserImpl();
        parser.parseWithStrategy(script, SCRIPT);
    }

    @Test(
        expected = ScriptParseException.class)
    public void shouldNotParseScriptWithReadBeforeConnect() throws Exception {

        String script =
                "# tcp.client.connect-then-close\n" + "read [0x01 0x02 0x03]\n" + "connect tcp://localhost:7788\n"
                        + "connected\n" + "close\n" + "closed\n";

        ScriptParserImpl parser = new ScriptParserImpl();
        parser.parseWithStrategy(script, SCRIPT);
    }

    @Test
    public void shouldParseReadOptionMask() throws Exception {

        String scriptFragment = "read option mask [0x01 0x02 0x03 0x04]";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadOptionNode actual = parser.parseWithStrategy(scriptFragment, READ_OPTION);

        AstReadOptionNode expected =
                new AstReadOptionNodeBuilder()
                    .setOptionType(OPTION_MASK)
                    .setOptionName("mask")
                    .setOptionValue(new byte[]{0x01, 0x02, 0x03, 0x04})
                .done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseReadOptionMaskExpression() throws Exception {

        String scriptFragment = "read option mask ${maskingKey}";

        ScriptParserImpl parser = new ScriptParserImpl();
        ExpressionFactory factory = parser.getExpressionFactory();
        ExpressionContext context = parser.getExpressionContext();

        AstReadOptionNode actual = parser.parseWithStrategy(scriptFragment, READ_OPTION);

        AstReadOptionNode expected =
                new AstReadOptionNodeBuilder()
                        .setOptionType(OPTION_MASK)
                        .setOptionName("mask")
                        .setOptionValue(factory.createValueExpression(context, "${maskingKey}", byte[].class),
                                parser.getExpressionContext()).done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseWriteOptionMask() throws Exception {

        String scriptFragment = "write option mask [0x01 0x02 0x03 0x04]";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstWriteOptionNode actual = parser.parseWithStrategy(scriptFragment, WRITE_OPTION);

        AstWriteOptionNode expected =
                new AstWriteOptionNodeBuilder()
                    .setOptionType(OPTION_MASK)
                    .setOptionName("mask")
                    .setOptionValue(new byte[]{0x01, 0x02, 0x03, 0x04})
                .done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseWriteOptionMaskExpression() throws Exception {

        String scriptFragment = "write option mask ${maskingKey}";

        ScriptParserImpl parser = new ScriptParserImpl();
        ExpressionFactory factory = parser.getExpressionFactory();
        ExpressionContext context = parser.getExpressionContext();

        AstWriteOptionNode actual = parser.parseWithStrategy(scriptFragment, WRITE_OPTION);

        AstWriteOptionNode expected =
                new AstWriteOptionNodeBuilder()
                        .setOptionType(OPTION_MASK)
                        .setOptionName("mask")
                        .setOptionValue(factory.createValueExpression(context, "${maskingKey}", byte[].class),
                                parser.getExpressionContext()).done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseReadConfig() throws Exception {

        String scriptFragment = "read test:config \"value1\" \"value2\"";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadConfigNode actual = parser.parseWithStrategy(scriptFragment, READ_CONFIG);

        AstReadConfigNode expected = new AstReadConfigNodeBuilder()
                .setType(CONFIG_CONFIG)
                .addMatcherExactText("value1")
                .addMatcherExactText("value2")
                .done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseWriteConfig() throws Exception {

        String scriptFragment = "write test:config \"configName\" [0x01 0x02 0x03 0x04]";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstWriteConfigNode actual = parser.parseWithStrategy(scriptFragment, WRITE_CONFIG);

        AstWriteConfigNode expected = new AstWriteConfigNodeBuilder()
                .setType(CONFIG_CONFIG)
                .addValue("configName")
                .addValue(new byte[]{0x01, 0x02, 0x03, 0x04})
                .done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseWriteConfigStringExpressionParameter() throws Exception {

        String scriptFragment = "write test:config \"configName\" ${'value'}";

        ScriptParserImpl parser = new ScriptParserImpl();
        ExpressionFactory factory = parser.getExpressionFactory();
        ExpressionContext context = parser.getExpressionContext();

        AstWriteConfigNode actual = parser.parseWithStrategy(scriptFragment, WRITE_CONFIG);

        AstWriteConfigNode expected = new AstWriteConfigNodeBuilder()
                .setType(CONFIG_CONFIG)
                .addValue("configName")
                .addValue(factory.createValueExpression(context, "${'value'}", Object.class),
                        parser.getExpressionContext())
                .done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseReadAdvise() throws Exception {

        String scriptFragment = "read advise test:advice [0x01 0x02 0x03 0x04]";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadAdviseNode actual = parser.parseWithStrategy(scriptFragment, READ_ADVISE);

        AstReadAdviseNode expected = new AstReadAdviseNodeBuilder()
                .setType(ADVISORY_ADVICE)
                .addValue(new byte[]{0x01, 0x02, 0x03, 0x04})
                .done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseReadAdviseStringExpressionParameter() throws Exception {

        String scriptFragment = "read advise test:advice ${'value'}";

        ScriptParserImpl parser = new ScriptParserImpl();
        ExpressionFactory factory = parser.getExpressionFactory();
        ExpressionContext context = parser.getExpressionContext();

        AstReadAdviseNode actual = parser.parseWithStrategy(scriptFragment, READ_ADVISE);

        AstReadAdviseNode expected = new AstReadAdviseNodeBuilder()
                .setType(ADVISORY_ADVICE)
                .addValue(factory.createValueExpression(context, "${'value'}", Object.class),
                        parser.getExpressionContext())
                .done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseWriteAdvise() throws Exception {

        String scriptFragment = "write advise test:advice [0x01 0x02 0x03 0x04]";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstWriteAdviseNode actual = parser.parseWithStrategy(scriptFragment, WRITE_ADVISE);

        AstWriteAdviseNode expected = new AstWriteAdviseNodeBuilder()
                .setType(ADVISORY_ADVICE)
                .addValue(new byte[]{0x01, 0x02, 0x03, 0x04})
                .done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseWriteAdviseStringExpressionParameter() throws Exception {

        String scriptFragment = "write advise test:advice ${'value'}";

        ScriptParserImpl parser = new ScriptParserImpl();
        ExpressionFactory factory = parser.getExpressionFactory();
        ExpressionContext context = parser.getExpressionContext();

        AstWriteAdviseNode actual = parser.parseWithStrategy(scriptFragment, WRITE_ADVISE);

        AstWriteAdviseNode expected = new AstWriteAdviseNodeBuilder()
                .setType(ADVISORY_ADVICE)
                .addValue(factory.createValueExpression(context, "${'value'}", Object.class),
                        parser.getExpressionContext())
                .done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseReadAdvised() throws Exception {

        String scriptFragment = "read advised test:advice \"value1\" \"value2\"";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadAdvisedNode actual = parser.parseWithStrategy(scriptFragment, READ_ADVISED);

        AstReadAdvisedNode expected = new AstReadAdvisedNodeBuilder()
                .setType(ADVISORY_ADVICE)
                .addMatcherExactText("value1")
                .addMatcherExactText("value2")
                .done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseWriteAdvised() throws Exception {

        String scriptFragment = "write advised test:advice \"value1\" \"value2\"";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstWriteAdvisedNode actual = parser.parseWithStrategy(scriptFragment, WRITE_ADVISED);

        AstWriteAdvisedNode expected = new AstWriteAdvisedNodeBuilder()
                .setType(ADVISORY_ADVICE)
                .addMatcherExactText("value1")
                .addMatcherExactText("value2")
                .done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseCapturingFixedLengthBytesMatcher2() throws Exception {

        String scriptFragment = "[(:capture){5}]";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstFixedLengthBytesMatcher actual = parser.parseWithStrategy(scriptFragment, FIXED_LENGTH_BYTES_MATCHER);

        AstFixedLengthBytesMatcher expected = new AstFixedLengthBytesMatcher(5, "capture", parser.getExpressionContext());

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseNamedPropertyWithLiteralText() throws Exception {

        String scriptFragment = "property location \"tcp://localhost:8000\"";

        ScriptParserImpl parser = new ScriptParserImpl();

        AstPropertyNode actual = parser.parseWithStrategy(scriptFragment, PROPERTY_NODE);

        AstPropertyNode expected =
                new AstPropertyNodeBuilder().setPropertyName("location").setPropertyValue("tcp://localhost:8000").done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseNamedPropertyWithLiteralBytes() throws Exception {

        String scriptFragment = "property location [0x00 0x01 0x02 0x03]";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstPropertyNode actual = parser.parseWithStrategy(scriptFragment, PROPERTY_NODE);

        AstPropertyNode expected =
                new AstPropertyNodeBuilder().setPropertyName("location").setPropertyValue(new byte[]{0x00, 0x01, 0x02, 0x03})
                        .done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseNamedPropertyWithExpression() throws Exception {

        String scriptFragment = "property location ${expression}";

        ScriptParserImpl parser = new ScriptParserImpl();

        ExpressionFactory factory = parser.getExpressionFactory();
        ExpressionContext context = parser.getExpressionContext();

        AstPropertyNode actual = parser.parseWithStrategy(scriptFragment, PROPERTY_NODE);

        ValueExpression expression = factory.createValueExpression(context, "${expression}", Object.class);

        AstPropertyNode expected =
                new AstPropertyNodeBuilder().setPropertyName("location")
                        .setPropertyValue(expression, parser.getExpressionContext()).done();

        assertEquals(expected, actual);
    }

}
