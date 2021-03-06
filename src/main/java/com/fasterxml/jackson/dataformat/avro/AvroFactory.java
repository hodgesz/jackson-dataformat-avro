package com.fasterxml.jackson.dataformat.avro;

import java.io.*;
import java.net.URL;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.format.InputAccessor;
import com.fasterxml.jackson.core.format.MatchStrength;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.dataformat.avro.deser.AvroParserImpl;

public class AvroFactory extends JsonFactory
{
    private static final long serialVersionUID = 1L;

    public final static String FORMAT_NAME_AVRO = "avro";
    
    /**
     * Bitfield (set of flags) of all parser features that are enabled
     * by default.
     */
    final static int DEFAULT_SMILE_PARSER_FEATURE_FLAGS = AvroParser.Feature.collectDefaults();

    /**
     * Bitfield (set of flags) of all generator features that are enabled
     * by default.
     */
    final static int DEFAULT_SMILE_GENERATOR_FEATURE_FLAGS = AvroGenerator.Feature.collectDefaults();

    /*
    /**********************************************************
    /* Configuration
    /**********************************************************
     */

    protected int _avroParserFeatures = DEFAULT_SMILE_PARSER_FEATURE_FLAGS;

    protected int _avroGeneratorFeatures = DEFAULT_SMILE_GENERATOR_FEATURE_FLAGS;
    
    /*
    /**********************************************************
    /* Factory construction, configuration
    /**********************************************************
     */

    /**
     * Default constructor used to create factory instances.
     * Creation of a factory instance is a light-weight operation,
     * but it is still a good idea to reuse limited number of
     * factory instances (and quite often just a single instance):
     * factories are used as context for storing some reused
     * processing objects (such as symbol tables parsers use)
     * and this reuse only works within context of a single
     * factory instance.
     */
    public AvroFactory() { this(null); }

    public AvroFactory(ObjectCodec oc) { super(oc); }

    /*                                                                                       
    /**********************************************************                              
    /* Versioned                                                                             
    /**********************************************************                              
     */

    @Override
    public Version version() {
        return ModuleVersion.instance.version();
    }
    
    /*
    /**********************************************************
    /* Format detection functionality
    /**********************************************************
     */
    
    @Override
    public String getFormatName()
    {
        return FORMAT_NAME_AVRO;
    }
    
    /**
     * Sub-classes need to override this method
     */
    @Override
    public MatchStrength hasFormat(InputAccessor acc) throws IOException
    {
        // TODO, if possible... probably isn't?
        return MatchStrength.INCONCLUSIVE;
    }
    
    /*
    /**********************************************************
    /* Configuration, parser settings
    /**********************************************************
     */

    /**
     * Method for enabling or disabling specified parser feature
     * (check {@link AvroParser.Feature} for list of features)
     */
    public final AvroFactory configure(AvroParser.Feature f, boolean state)
    {
        if (state) {
            enable(f);
        } else {
            disable(f);
        }
        return this;
    }

    /**
     * Method for enabling specified parser feature
     * (check {@link AvroParser.Feature} for list of features)
     */
    public AvroFactory enable(AvroParser.Feature f) {
        _avroParserFeatures |= f.getMask();
        return this;
    }

    /**
     * Method for disabling specified parser features
     * (check {@link AvroParser.Feature} for list of features)
     */
    public AvroFactory disable(AvroParser.Feature f) {
        _avroParserFeatures &= ~f.getMask();
        return this;
    }

    /**
     * Checked whether specified parser feature is enabled.
     */
    public final boolean isEnabled(AvroParser.Feature f) {
        return (_avroParserFeatures & f.getMask()) != 0;
    }

    /*
    /**********************************************************
    /* Configuration, generator settings
    /**********************************************************
     */

    /**
     * Method for enabling or disabling specified generator feature
     * (check {@link AvroGenerator.Feature} for list of features)
     *
     * @since 1.2
     */
    public final AvroFactory configure(AvroGenerator.Feature f, boolean state) {
        if (state) {
            enable(f);
        } else {
            disable(f);
        }
        return this;
    }


    /**
     * Method for enabling specified generator features
     * (check {@link AvroGenerator.Feature} for list of features)
     */
    public AvroFactory enable(AvroGenerator.Feature f) {
        _avroGeneratorFeatures |= f.getMask();
        return this;
    }

    /**
     * Method for disabling specified generator feature
     * (check {@link AvroGenerator.Feature} for list of features)
     */
    public AvroFactory disable(AvroGenerator.Feature f) {
        _avroGeneratorFeatures &= ~f.getMask();
        return this;
    }

    /**
     * Check whether specified generator feature is enabled.
     */
    public final boolean isEnabled(AvroGenerator.Feature f) {
        return (_avroGeneratorFeatures & f.getMask()) != 0;
    }
    
    /*
    /**********************************************************
    /* Overridden parser factory methods
    /**********************************************************
     */

    @Override
    public AvroParser createParser(File f)
        throws IOException, JsonParseException
    {
        return _createParser(new FileInputStream(f), _createContext(f, true));
    }

    @Override
    public AvroParser createParser(URL url)
        throws IOException, JsonParseException
    {
        return _createParser(_optimizedStreamFromURL(url), _createContext(url, true));
    }

    @Override
    public AvroParser createParser(InputStream in)
        throws IOException, JsonParseException
    {
        return _createParser(in, _createContext(in, false));
    }

    //public JsonParser createParser(Reader r)
    
    @Override
    public AvroParser createParser(byte[] data)
        throws IOException, JsonParseException
    {
        IOContext ctxt = _createContext(data, true);
        return _createParser(data, 0, data.length, ctxt);
    }
    
    @Override
    public AvroParser createParser(byte[] data, int offset, int len)
        throws IOException, JsonParseException
    {
        return _createParser(data, offset, len, _createContext(data, true));
    }

    /*
    /**********************************************************
    /* Overridden generator factory methods
    /**********************************************************
     */
    
    /**
     *<p>
     * note: co-variant return type
     */
    @Override
    public AvroGenerator createGenerator(OutputStream out, JsonEncoding enc)
        throws IOException
    {
        return createGenerator(out);
    }

    /**
     * Since Avro format always uses UTF-8 internally, no encoding need
     * to be passed to this method.
     */
    @Override
    public AvroGenerator createGenerator(OutputStream out) throws IOException
    {
        // false -> we won't manage the stream unless explicitly directed to
        IOContext ctxt = _createContext(out, false);
        return _createGenerator(out, ctxt);
    }
    
    /*
    /******************************************************
    /* Overridden internal factory methods
    /******************************************************
     */

    //protected IOContext _createContext(Object srcRef, boolean resourceManaged)

    /**
     * Overridable factory method that actually instantiates desired
     * parser.
     */
    @Override
    protected AvroParser _createParser(InputStream in, IOContext ctxt)
        throws IOException, JsonParseException
    {
        return new AvroParserImpl(ctxt, _parserFeatures, _avroParserFeatures,
                _objectCodec, in);
    }

    /**
     * Overridable factory method that actually instantiates desired
     * parser.
     */
    @Override
    protected JsonParser _createParser(Reader r, IOContext ctxt)
        throws IOException, JsonParseException
    {
        throw new UnsupportedOperationException("Can not create generator for non-byte-based target");
    }

    /**
     * Overridable factory method that actually instantiates desired
     * parser.
     */
    @Override
    protected AvroParser _createParser(byte[] data, int offset, int len, IOContext ctxt)
        throws IOException, JsonParseException
    {
        return new AvroParserImpl(ctxt, _parserFeatures, _avroParserFeatures,
                _objectCodec, data, offset, len);
    }

    /**
     * Overridable factory method that actually instantiates desired
     * generator.
     */
    @Override
    protected JsonGenerator _createGenerator(Writer out, IOContext ctxt)
        throws IOException
    {
        throw new UnsupportedOperationException("Can not create generator for non-byte-based target");
    }

    //public BufferRecycler _getBufferRecycler()

    @Override
    protected Writer _createWriter(OutputStream out, JsonEncoding enc, IOContext ctxt) throws IOException
    {
        throw new UnsupportedOperationException("Can not create generator for non-byte-based target");
    }
    
    /*
    /**********************************************************
    /* Internal methods
    /**********************************************************
     */
    
    protected AvroGenerator _createGenerator(OutputStream out, IOContext ctxt)
        throws IOException
    {
        int feats = _avroGeneratorFeatures;
        AvroGenerator gen = new AvroGenerator(ctxt, _generatorFeatures, feats,
                _objectCodec, out);
        return gen;
    }
}