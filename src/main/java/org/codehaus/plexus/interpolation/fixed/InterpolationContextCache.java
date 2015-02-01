/*
    Copyright 2015 the original author or authors

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package org.codehaus.plexus.interpolation.fixed;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;

/**
 * @author Kristian Rosenvold
 */
public class InterpolationContextCache
{
    private final LinkedHashMap<String, Object> values = new LinkedHashMap<String, Object>();

    private final MessageDigest md = getMesageDigestInstance();

    private final File cacheFile;

    private boolean cacheable = true;

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    public InterpolationContextCache( File cacheFile )
    {
        this.cacheFile = cacheFile;
    }

    /**
     * Indicates if the cached interpolation expressions resolve to the same values in the new interpolator
     *
     * @param interpolator        The new interpolator
     * @param interpolationState  The interpolation state, will be cleared completion of this method
     * @return true if the new interpolator resolves to the same values as the old one. If false, the caller should
     *              always re-interpolate, no matter what
     * @throws IOException
     */
    public boolean resolvesToSameValues( FixedValueSource interpolator, InterpolationState interpolationState )
        throws IOException
    {
        if (!hasCacheFile()) return false;
        BufferedReader br = new BufferedReader( new FileReader( cacheFile ) );
        String sha1 = br.readLine();
        String key;
        Object value;

        while ( ( key = br.readLine() ) != null )
        {
            value = interpolator.getValue( key, interpolationState );
            addToHash( value );
        }

        interpolationState.clear();
        return getHexHash().equals( sha1 );
    }

    private static MessageDigest getMesageDigestInstance()
    {
        try
        {
            return MessageDigest.getInstance( "SHA-1" );
        }
        catch ( NoSuchAlgorithmException e )
        {
            throw new RuntimeException( e );
        }
    }

    /**
     * Registers the result of an interpolation
     *
     * @param key   The interpolation key
     * @param value The resolved value
     */

    public InterpolationContextCache putValue( String key, Object value )
    {
        Object existing = values.get( key );
        if ( existing != null )
        {
            if ( !existing.equals( value ) )
            {
                cacheable = false;
            }
        }
        else
        {
            values.put( key, value );
        }
        return this;
    }

    public InterpolationContextCache store()
        throws IOException
    {
        if ( !cacheable )
        {
            if ( cacheFile.exists() )
            {
                FileUtils.deleteQuietly( cacheFile );
            }
            return this;
        }
        FileOutputStream fos = new FileOutputStream( cacheFile );
        Writer writer = new OutputStreamWriter( fos, UTF_8 );
        writer.write( getSha1( values.values() ) );
        writer.write( '\n' );
        for ( String s : values.keySet() )
        {
            writer.write( s );
            writer.write( '\n' );
        }
        writer.close();
        return this;
    }

    /**
     * Inquires if we have to filter no matter what, based on file attributes
     * @param targetFile The filtered output file
     * @return True if the source file is newer than the target, or the target does not exist
     */
    public boolean mustFilterDueToUpdateCheck( File sourceFile, File targetFile )
    {
        return !targetFile.exists() || sourceFile.lastModified() > targetFile.lastModified();
    }

    /**
     * Indicates if we have a cache file
     * @return True if we have a cache file
     */
    private boolean hasCacheFile( ){
        return cacheFile.exists();
    }


    private String getSha1( Iterable values )
    {
        for ( Object value : values )
        {
            addToHash( value );
        }

        return getHexHash();
    }

    private String getHexHash()
    {
        byte[] sha1hash = md.digest();
        md.reset();
        return asHexString( sha1hash );
    }

    private void addToHash( Object value )
    {
        // dont really care which encoding is used as long as we're consistent
        md.update( value.toString().getBytes( UTF_8 ) );
    }

    @SuppressWarnings( "checkstyle:magicnumber" )
    private static String asHexString( byte[] bytes )
    {
        if ( bytes == null )
        {
            return null;
        }
        final StringBuilder result = new StringBuilder( 2 * bytes.length );
        for ( byte b : bytes )
        {
            result.append( HEX.charAt( ( b & 0xF0 ) >> 4 ) ).append( HEX.charAt( ( b & 0x0F ) ) );
        }
        return result.toString();
    }

    private static final String HEX = "0123456789ABCDEF";

    public File getCacheFile()
    {
        return cacheFile;
    }
}
