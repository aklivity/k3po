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
grammar Robot;

/* Parser rules */

// A robot script is comprised of a list of commands, events and barriers for
// each stream. Note that we deliberately allow empty scripts, in order to
// handle scripts which are comprised of nothing but empty lines and comments.

scriptNode
    : (propertyNode | streamNode)* EOF
    ;

propertyNode
    : PropertyKeyword name=propertyName value=propertyValue
    ;

propertyName
    : Name
    ;

propertyValue
    : writeValue
    ;

optionName
    : QualifiedName
    | Name
    ;

streamNode
    : acceptNode
    | acceptedNode
    | rejectedNode
    | connectNode
    ;

acceptNode
    : AcceptKeyword
      (AwaitKeyword await=Name)?
      acceptURI=location (AsKeyword as=Name)?
      (NotifyKeyword notify=Name)?
      acceptOption*
      serverStreamableNode*
    ;

acceptOption
    : OptionKeyword optionName writeValue
    ;

acceptedNode
    : AcceptedKeyword ( text=Name )? streamableNode+
    ;

rejectedNode
    : RejectedKeyword ( text=Name )? rejectableNode*
    ;

connectNode
    : ConnectKeyword
      (AwaitKeyword await=Name)?
      connectURI=location
      connectOption*
      streamableNode+
    ;

connectOption
    : OptionKeyword optionName writeValue
    ;

serverStreamableNode
    : barrierNode 
    | serverEventNode
    | serverCommandNode
    | optionNode
    ;
    
optionNode
    : readOptionNode
    | writeOptionNode
    ;

readOptionNode
    : ReadKeyword OptionKeyword optionName writeValue
    ;

writeOptionNode
    : WriteKeyword OptionKeyword optionName writeValue
    ;

serverCommandNode
    : unbindNode
    | closeNode
    ;

serverEventNode
    : openedNode
    | boundNode
    | childOpenedNode
    | childClosedNode
    | unboundNode
    | closedNode
    ;

streamableNode
    : barrierNode
    | eventNode
    | commandNode
    | optionNode
    ;

rejectableNode
    : barrierNode
    | readConfigNode
    ;

commandNode
    : connectAbortNode
    | writeConfigNode
    | writeNode
    | writeFlushNode
    | writeCloseNode
    | writeAbortNode
    | readAbortNode
    | writeAdviseNode
    | readAdviseNode
    | closeNode
    ;

eventNode
    : connectAbortedNode
    | openedNode
    | boundNode
    | readConfigNode
    | readNode
    | readClosedNode
    | readAbortedNode
    | writeAbortedNode
    | readAdvisedNode
    | writeAdvisedNode
    | disconnectedNode
    | unboundNode
    | closedNode
    | connectedNode
    ;

barrierNode
    : readAwaitNode
    | readNotifyNode
    | writeAwaitNode
    | writeNotifyNode
    ;

connectAbortNode
    : ConnectKeyword AbortKeyword
    ;

connectAbortedNode
    : ConnectKeyword AbortedKeyword
    ;

closeNode
    : CloseKeyword
    ;

writeFlushNode
    : WriteKeyword FlushKeyword
    ;

writeCloseNode
    : WriteKeyword CloseKeyword
    ;

writeAbortNode
    : WriteKeyword AbortKeyword
    ;

writeAbortedNode
    : WriteKeyword AbortedKeyword
    ;

writeAdviseNode
    : WriteKeyword AdviseKeyword QualifiedName writeValue*
    ;

writeAdvisedNode
    : WriteKeyword AdvisedKeyword QualifiedName matcher* MissingKeyword?
    ;

disconnectNode
    : DisconnectKeyword
    ;

unbindNode
    : UnbindKeyword
    ;

writeConfigNode
    : WriteKeyword QualifiedName writeValue*
    ;

writeNode
    : WriteKeyword writeValue+
    ;

childOpenedNode
    : ChildKeyword OpenedKeyword
    ;

childClosedNode
    : ChildKeyword ClosedKeyword
    ;

boundNode
    : BoundKeyword
    ;

closedNode
    : ClosedKeyword
    ;

connectedNode
    : ConnectedKeyword
    ;

disconnectedNode
    : DisconnectedKeyword
    ;

openedNode
    : OpenedKeyword
    ;

readAbortNode
    : ReadKeyword AbortKeyword
    ;

readAbortedNode
    : ReadKeyword AbortedKeyword
    ;

readAdviseNode
    : ReadKeyword AdviseKeyword QualifiedName writeValue*
    ;

readAdvisedNode
    : ReadKeyword AdvisedKeyword QualifiedName matcher* MissingKeyword?
    ;

readClosedNode
    : ReadKeyword ClosedKeyword
    ;

readConfigNode
    : ReadKeyword QualifiedName matcher* MissingKeyword?
    ;

readNode
    : ReadKeyword matcher+
    ;

unboundNode
    : UnboundKeyword
    ;

readAwaitNode
    : ReadKeyword AwaitKeyword Name
    ;

readNotifyNode
    : ReadKeyword NotifyKeyword Name
    ;
    
writeAwaitNode
    : WriteKeyword AwaitKeyword Name
    ;

writeNotifyNode
    : WriteKeyword NotifyKeyword Name
    ;

matcher
    : exactTextMatcher
    | exactBytesMatcher
    | numberMatcher
    | regexMatcher
    | expressionMatcher
    | fixedLengthBytesMatcher
    | variableLengthBytesMatcher
    ;

exactTextMatcher
    : text=TextLiteral
    ;

exactBytesMatcher
    : bytes=BytesLiteral
    ;

numberMatcher
    : ByteKeyword byteLiteral=HexLiteral
    | ShortKeyword shortLiteral=(SignedDecimalLiteral | DecimalLiteral | HexLiteral) 's'?
    | ShortKeyword? shortLiteral=(SignedDecimalLiteral | DecimalLiteral | HexLiteral) 's'
    | IntKeyword? intLiteral=(SignedDecimalLiteral | DecimalLiteral | HexLiteral)
    | LongKeyword longLiteral=(SignedDecimalLiteral | DecimalLiteral | HexLiteral) 'L'?
    | LongKeyword? longLiteral=(SignedDecimalLiteral | DecimalLiteral | HexLiteral) 'L'
    ;

regexMatcher
    :  regex=RegexLiteral
    ;

expressionMatcher
    : expression=ExpressionLiteral
    ;

fixedLengthBytesMatcher
    : '[0..' lastIndex=DecimalLiteral ']'
    | '([0..' lastIndex=DecimalLiteral ']' capture=CaptureLiteral ')'
    | '[(' capture=CaptureLiteral '){' lastIndex=DecimalLiteral '}]'
    |  '(byte' byteCapture=CaptureLiteral ')'
    |  '(short' shortCapture=CaptureLiteral ')'
    |  '(int' intCapture=CaptureLiteral ')'
    |  '(long' longCapture=CaptureLiteral ')'
    ;
    
variableLengthBytesMatcher
    : '[0..' length=ExpressionLiteral ']'
    | '([0..' length=ExpressionLiteral ']' capture=CaptureLiteral ')'
    ;

writeValue
    : literalText
    | literalBytes
    | literalByte
    | literalShort
    | literalInteger
    | literalLong
    | expressionValue
    ;

literalText
    : literal=TextLiteral
    ;

literalBytes
    : literal=BytesLiteral
    ;

literalByte
    : ByteKeyword literal=(SignedDecimalLiteral | DecimalLiteral | HexLiteral)
    ;

literalShort
    : ShortKeyword literal=(SignedDecimalLiteral | DecimalLiteral | HexLiteral) 's'?
    | ShortKeyword? literal=(SignedDecimalLiteral | DecimalLiteral | HexLiteral) 's' 
    ;

literalInteger
    : IntKeyword? literal=(SignedDecimalLiteral | DecimalLiteral | HexLiteral) 
    ;

literalLong
    : LongKeyword literal=(SignedDecimalLiteral | DecimalLiteral | HexLiteral) 'L'?
    | LongKeyword? literal=(SignedDecimalLiteral | DecimalLiteral | HexLiteral) 'L'
    ;

expressionValue
    : expression=ExpressionLiteral
    ;

location
    : literalText
    | expressionValue
    ;

SignedDecimalLiteral
    :  Plus DecimalLiteral
    |  Minus DecimalLiteral
//    |  DecimalLiteral
    ;

AbortKeyword
    : 'abort'
    ;

AbortedKeyword
    : 'aborted'
    ;

AcceptKeyword
    : 'accept'
    ;

AcceptedKeyword
    : 'accepted'
    ;

AdviseKeyword
    : 'advise'
    ;

AdvisedKeyword
    : 'advised'
    ;

AsKeyword
    : 'as'
    ;

AwaitKeyword
    : 'await'
    ;

BindKeyword
    : 'bind'
    ;

BoundKeyword
    : 'bound'
    ;

ByteKeyword
    : 'byte'
    ;

ChildKeyword
    : 'child'
    ;

CloseKeyword
    : 'close'
    ;

ClosedKeyword
    : 'closed'
    ;

ConfigKeyword
    : 'config'
    ;

ConnectKeyword
    : 'connect'
    ;

ConnectedKeyword
    : 'connected'
    ;

DisconnectKeyword
    : 'disconnect'
    ;

DisconnectedKeyword
    : 'disconnected'
    ;

IntKeyword
    : 'int'
    ;

FlushKeyword
    : 'flush'
    ;

LongKeyword
    : 'long'
    ;

MissingKeyword
    : 'missing'
    ;

NotifyKeyword
    : 'notify'
    ;

OpenedKeyword
    : 'opened'
    ;

OptionKeyword
    : 'option'
    ;

PropertyKeyword
    : 'property'
    ;

ReadKeyword
    : 'read'
    ;

RejectedKeyword
    : 'rejected'
    ;

ShortKeyword
    : 'short'
    ;

UnbindKeyword
    : 'unbind'
    ;

UnboundKeyword
    : 'unbound'
    ;

WriteKeyword
    : 'write'
    ;

// URI cannot begin with any of our data type delimiters, and MUST contain a colon.
URILiteral
    : Letter (Letter | '+')+ ':' '/'
      (Letter | ':' | ';' | '/' | '=' | '.' | DecimalLiteral | '?' | '&' | '%' | '-' | ',' | '*')+
//      ~('"' | '/' | ']' | '}')
    ;

CaptureLiteral
    : ':' Identifier
    ;

ExpressionLiteral
    : '${' ~('}')+ '}'
    ;

RegexLiteral
  : '/' PatternLiteral '/'
// ( RegexNamedGroups+ '/' )?
  ;

//RegexNamedGroups
//  :  '(' CaptureLiteral RegexNamedGroups* ')'
//  ;

fragment
PatternLiteral
    : (~('/' | '\r' | '\n') | '\\' '/')+
    ;

BytesLiteral
    : '[' (' ')? (ByteLiteral (' ')*)+ ']'
    ;

fragment
ByteLiteral
    : HexPrefix HexDigit HexDigit
    ;

HexLiteral
    : HexPrefix HexDigit ('_'? HexDigit)*
    ;

fragment
HexPrefix
    : '0'  ('x' | 'X')
    ;

fragment
HexDigit
    : (Digit | 'a'..'f' | 'A'..'F')
    ;


Plus
    : '+'
    ;

Minus
    : '-'
    ;

DecimalLiteral
    : Number
    ;

fragment
Number
   : Digit+
   ;

fragment
Digit
    : '0'..'9'
    ;

TextLiteral
    : '"' (EscapeSequence | ~('\\' | '\r' | '\n' | '"'))+ '"'
    | '\'' (EscapeSequence | ~('\\' | '\r' | '\n' | '\''))+ '\''
    ;
    
// Any additions to the escaping need to be accounted for in
// ScriptParseStrategy.escapeString(String toEscape);
fragment
EscapeSequence
    : '\\' ('b' | 'f' | 'r' | 'n' | 't' | '"' | '\'' | '\\' )
    ;

Name
    : Identifier
    ;

QualifiedName
    : Identifier ':' Identifier ('.' Identifier)*
    ;

fragment
Identifier
    : Letter (Digit | Minus | Letter)*
    ;

fragment
Letter
    : '\u0024'
    | '\u0041'..'\u005a'
    | '\u005f'
    | '\u0061'..'\u007a'
    | '\u00c0'..'\u00d6'
    | '\u00d8'..'\u00f6'
    | '\u00f8'..'\u00ff'
    | '\u0100'..'\u1fff'
    | '\u3040'..'\u318f'
    | '\u3300'..'\u337f'
    | '\u3400'..'\u3d2d'
    | '\u4e00'..'\u9fff'
    | '\uf900'..'\ufaff'
    ;

WS    : (' ' | '\r' | '\n' | '\t' | '\u000C')+ -> skip;

LineComment
    : '#' ~('\n' | '\r')* '\r'? '\n' -> skip;
