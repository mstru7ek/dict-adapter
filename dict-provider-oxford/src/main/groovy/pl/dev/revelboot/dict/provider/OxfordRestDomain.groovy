package pl.dev.revelboot.dict.provider

import groovy.transform.ToString

class OxfordRestDomain {
}

@ToString
class Wordlist {
    Metadata metadata
    WordSearchEntity[] results
}

@ToString
class WordSearchEntity {
    String id
    String inflection_id
    Double score
    String word
    String matchString
    String matchType
    String region
}

@ToString
class Metadata {
    String sourceLanguage
    Integer limit
    Integer offset
    String provider
    Integer total
}

@ToString
class Thesaurus {
    Metadata metadata
    HeadwordThesaurus[] results
}

@ToString
class HeadwordThesaurus {
    String id
    String language
    ThesaurusLexicalEntry[] lexicalEntries
    String type
    String word
}

@ToString
class ThesaurusLexicalEntry {
    ThesaurusEntry[] entries
    String language
    String lexicalCategory
    String text
    VariantForm[] variantForms
}

@ToString
class ThesaurusEntry {
    String homographNumber
    ThesaurusSense[] senses
    VariantForm[] variantForms
}

@ToString
class ThesaurusSense {
    SynonymsAntonyms antonyms
    String[] domains
    Example[] examples
    String id
    String[] regions
    String[] registers
    ThesaurusSense[] subsenses
    SynonymsAntonyms[] synonyms
}

@ToString
class VariantForm {
    String[] regions
    String text
}

@ToString
class SynonymsAntonyms {
    String[] domains
    String id
    String language
    String[] regions
    String[] registers
    String text
}

@ToString
class Example {
    String[] definitions
    String[] domains
    CategorizedText[] notes
    String[] regions
    String[] registers
    String[] senseIds
    String text
    Translation[] translations
}

@ToString
class CategorizedText {
    String id
    String text
    String type
}

@ToString
class Translation {
    String[] domains
    GrammaticalFeature[] grammaticalFeatures
    String language
    CategorizedText[] notes
    String[] regions
    String[] registers
    String text
}

@ToString
class GrammaticalFeature {
    String text
    String type
}

@ToString
class RetrieveEntry {
    Metadata metadata
    HeadwordEntry[] results
}

@ToString
class HeadwordEntry {
    String id
    String language
    LexicalEntry[] lexicalEntries
    Pronunciation[] pronunciations
    String type
    String word
}

@ToString
class LexicalEntry {
    RelatedEntries[] derivativeOf
    RelatedEntries[] derivatives
    Entry[] entries
    GrammaticalFeature[] grammaticalFeatures
    String language
    String lexicalCategory
    CategorizedText[] notes
    Pronunciation[] pronunciations
    String text
    VariantForm variantForms
}

@ToString
class Entry {
    String[] etymologies
    GrammaticalFeature[] grammaticalFeatures
    String homographNumber
    CategorizedText[] notes
    Pronunciation[] pronunciations
    Sense[] senses
    VariantForm[] variantForms
}

@ToString
class Pronunciation {
    String audioFile
    String[] dialects
    String phoneticNotation
    String phoneticSpelling
    String[] regions
}

@ToString
class RelatedEntries {
    String[] domains
    String id
    String language
    String[] regions
    String[] registers
    String text
}

@ToString
class Sense {
    String[] crossReferenceMarkers
    CrossReference[] crossReferences
    String[] definitions
    String[] domains
    Example[] examples
    String id
    CategorizedText[] notes
    Pronunciation[] pronunciations
    String[] regions
    String[] registers
    String[] short_definitions
    Sense[] subsenses
    ThesaurusLink[] thesaurusLinks
    Translation[] translations
    VariantForm[] variantForms
}

@ToString
class ThesaurusLink {
    String entry_id
    String sense_id
}
@ToString
class CrossReference {
    String id
    String text
    String type
}
