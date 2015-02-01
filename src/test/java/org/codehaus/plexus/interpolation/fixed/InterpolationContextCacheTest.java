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

import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.codehaus.plexus.interpolation.fixed.FixedStringSearchInterpolator.create;
import static org.junit.Assert.*;

public class InterpolationContextCacheTest
{

    private static final int MINIMAL_FS_TIMESTAMP_GRANULARITY = 2000;

    private File testDir = new File( "target/output" );

    {
        //noinspection ResultOfMethodCallIgnored
        testDir.mkdirs();
    }

    private final String initalpayload = "a${v1}payload\n";

    private final String expectedPayload = "aabcpayload\n";

    @Test
    public void basicStoreOfAttrs()
        throws Exception
    {
        assertContents( getBasicCache( new File( testDir, "testStore.txt" ) ).store() );
    }

    @Test
    public void repeatedValuesShouldProduceSameResults()
        throws Exception
    {
        assertContents( getBasicCache( new File( testDir, "testStore2.txt" ) ).putValue( "v1", "abc" ).store() );
    }


    @Test
    public void shouldDeletesExistingWhenNowUncacheableDoesNotStore()
        throws Exception
    {
        InterpolationContextCache ic = getBasicCache( new File( testDir, "testStore3.txt" ) );
        ic.store();
        assertTrue( ic.getCacheFile().exists() );
        ic.putValue( "v1", "abdc" );
        ic.store(); // removes existing file
        assertFalse( ic.getCacheFile().exists() );
        ic.store(); // does not store
        assertFalse( ic.getCacheFile().exists() );
    }

    @Test
    public void shouldNotStoreWhenUncacheable()
        throws Exception
    {
        InterpolationContextCache ic = getBasicCache( new File( testDir, "testStore4.txt" ) );
        ic.putValue( "v1", "abdc" );
        ic.store(); // does not store
        assertFalse( ic.getCacheFile().exists() );
    }

    @Test
    public void shouldBeAbleToResolveToSame()
        throws IOException
    {
        InterpolationContextCache ic = getBasicCache( new File( testDir, "testStore5.txt" ) );
        ic.store();

        assertTrue( ic.resolvesToSameValues( getMatchingValueSource(), new InterpolationState() ) );
    }

    @Test
    public void shouldNotBeAbleToResolveToSame()
        throws IOException
    {
        InterpolationContextCache ic = getBasicCache( new File( testDir, "testStore6.txt" ) );
        ic.store();

        assertFalse( ic.resolvesToSameValues( getNonMatchingValueSource(), new InterpolationState() ) );
    }

    @Test
    public void needsToFilter()
        throws IOException
    {
        File cacheFile = interpolateOnceToWriteCacheFile( "needsToFilter" );

        InterpolationContextCache nextRun = getBasicCache( cacheFile );

        FixedStringSearchInterpolator nextInterpolator = create( nextRun, getNonMatchingValueSource() );

        assertFalse( nextRun.resolvesToSameValues( nextInterpolator, new InterpolationState() ) );
    }

    @Test
    public void doesNotNeedToFilter()
        throws IOException
    {
        File cacheFile = interpolateOnceToWriteCacheFile( "needsToFilter" );

        InterpolationContextCache nextRun = getBasicCache( cacheFile );

        FixedStringSearchInterpolator nextInterpolator = create( nextRun, getMatchingValueSource() );

        assertTrue( nextRun.resolvesToSameValues( nextInterpolator, new InterpolationState() ) );
    }

    @Test
    public void mustFilterNoMatterWhatWhenTimeStampChanges()
        throws IOException
    {
        long timeStamp = System.currentTimeMillis();
        File sourceFile = writeTestFile( "shouldNotNeedToFilter", initalpayload, timeStamp );
        File target = writeTestFile( "shouldNotNeedToFilter.interpolated", expectedPayload, timeStamp );

        InterpolationContextCache nextRun = getBasicCache( interpolateOnceToWriteCacheFile( "needsToFilter" ) );

        assertFalse( nextRun.mustFilterDueToUpdateCheck( sourceFile, target ) );

        assertTrue( sourceFile.setLastModified( timeStamp + MINIMAL_FS_TIMESTAMP_GRANULARITY ) );

        assertTrue( nextRun.mustFilterDueToUpdateCheck( sourceFile, target ) );
    }

    @Test
    public void missingCacheFileAlways()
        throws IOException
    {
        InterpolationContextCache nextRun = getBasicCache( new File( "NonExisting" ) );

        FixedStringSearchInterpolator nextInterpolator = create( nextRun, getNonMatchingValueSource() );

        assertFalse( nextRun.resolvesToSameValues( nextInterpolator, new InterpolationState() ) );
    }

    @Test
    public void mustFilterNoMatterWithoutTargetFile()
        throws IOException
    {
        long timeStamp = System.currentTimeMillis();
        File sourceFile = writeTestFile( "shouldNotNeedToFilter", initalpayload, timeStamp );
        File target = new File("doesNotExist" );


        InterpolationContextCache nextRun = getBasicCache( new File("cacheFile" ));

        assertTrue( nextRun.mustFilterDueToUpdateCheck( sourceFile, target ) );
    }



    private File interpolateOnceToWriteCacheFile( String cacheFileName )
        throws IOException
    {
        File cacheFile = createTestFile( cacheFileName + ".cacheFile" );
        InterpolationContextCache basicCache = getBasicCache( cacheFile );

        FixedStringSearchInterpolator interpolator = create( basicCache, getMatchingValueSource() );

        InterpolationState interpolationState = new InterpolationState();
        String actual = interpolator.interpolate( initalpayload, interpolationState );
        assertEquals( expectedPayload, actual );

        basicCache.store();
        return cacheFile;
    }

    @SuppressWarnings( "ResultOfMethodCallIgnored" )
    private File writeTestFile( String fileName, String a1payload, long timeStamp )
        throws IOException
    {
        File file = createTestFile( fileName );
        FileOutputStream fos = new FileOutputStream( file );
        fos.write( a1payload.getBytes( "utf-8" ));
        fos.close();
        file.setLastModified( timeStamp );
        return file;

    }

    private File createTestFile( String fileName )
    {
        return new File( testDir, fileName );
    }

    private InterpolationContextCache getBasicCache( File cachefile )
    {
        InterpolationContextCache ic = new InterpolationContextCache( cachefile );
        ic.putValue( "v1", "abc" );
        ic.putValue( "v2", "cde" );
        return ic;
    }

    private FixedValueSource getMatchingValueSource()
    {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put( "v1", "abc" );
        values.put( "v2", "cde" );
        return new MapBasedValueSource( values );
    }

    private FixedValueSource getNonMatchingValueSource()
    {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put( "v1", "aDDc" );
        values.put( "v2", "cde" );
        return new MapBasedValueSource( values );
    }

    private void assertContents( InterpolationContextCache ic )
        throws IOException
    {
        BufferedReader br = new BufferedReader( new FileReader( ic.getCacheFile() ) );
        assertEquals( "D9F4E651F88121479D8C6CDA4441ECBA65687415", br.readLine() );
        assertEquals( "v1", br.readLine() );
        assertEquals( "v2", br.readLine() );
        br.close();
    }

}