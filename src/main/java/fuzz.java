import org.json.JSONArray;
import org.json.JSONObject;

import com.codesnippets4all.json.parsers.JSONParser;
import com.codesnippets4all.json.parsers.JsonParserFactory;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.util.JSON;

import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;

public class fuzz
{

static boolean print;

static final List< Character > charList = new ArrayList<>();
static
{
	for( int i = ( int )' '; i <= ( int )'~'; ++i )
	{
		if( i != ( int )'#' )
		{
			charList.add( ( char )i );
		}
	}
}

public static void main( String[] args ) throws Exception
{
	final List< Function< String, Boolean > > fList = new ArrayList<>();

	fList.add( ( s ) ->
	{
		final String name = "JSON.org:";
		try
		{
			final JSONArray arr = new JSONArray( s );
			p( name + "OK" );
			return true;
		}
		catch( Exception e )
		{
			p( name + ":" + e.toString().replaceAll( "(\\r|\\n)", "" ) );
			return false;
		}
	} );
	fList.add( ( s ) ->
	{
		final String name = "GSON:";
		try
		{
			Gson g = new Gson();
			List l = g.fromJson( s, List.class );
			p( name + "OK" );
			return true;
		}
		catch( Exception e )
		{
			p( name + ":" + e.toString().replaceAll( "(\\r|\\n)", "" ) );
			return false;
		}
	} );
	fList.add( ( s ) ->
	{
		final String name = "Quick-JSON:";
		try
		{
			JsonParserFactory factory = JsonParserFactory.getInstance();
			JSONParser parser = factory.newJsonParser();
			Map jsonMap = parser.parseJson( "{\"a\":" + s + "}" );
			p( name + "OK" );
			return true;
		}
		catch( Exception e )
		{
			p( name + ":" + e.toString().replaceAll( "(\\r|\\n)", "" ) );
			return false;
		}
	} );
	fList.add( ( s ) ->
	{
		final String name = "JSONIC:";
		try
		{
			net.arnx.jsonic.JSON.decode( s );
			p( name + "OK" );
			return true;
		}
		catch( Exception e )
		{
			p( name + ":" + e.toString().replaceAll( "(\\r|\\n)", "" ) );
			return false;
		}
	} );
	fList.add( ( s ) ->
	{
		final String name = "MongoDB:";
		try
		{
			JSON.parse( s );
			p( name + "OK" );
			return true;
		}
		catch( Exception e )
		{
			p( name + ":" + e.toString().replaceAll( "(\\r|\\n)", "" ) );
			return false;
		}
	} );

	final String[] array1 = new String[ fList.size() ];
	Arrays.fill( array1, null );
	final String[] array2 = new String[ fList.size() ];
	Arrays.fill( array2, null );

	final boolean[] flag1 = new boolean[] { false };
	final boolean[] flag2 = new boolean[] { false };

	final Function< Integer, Integer > func = ( i ) ->
	{
		while( true )
		{
			final String s = "[" + getStr1() + "]";
			final BitSet bs = new BitSet();
			for( int f = 0; f < fList.size(); ++f )
			{
				bs.set( f, fList.get( f ).apply( s ) );
			}
			if( !flag1[ 0 ] && bs.cardinality() == 1 )
			{
				synchronized( flag1 )
				{
					array1[ bs.nextSetBit( 0 ) ] = s;
					if( Arrays.stream( array1 ).allMatch( str -> str != null ) )
					{
						flag1[ 0 ] = true;
					}
				}
			}
			/*
			else if( !flag2[ 0 ] && bs.cardinality() == fList.size() - 1 )
			{
				synchronized( flag2 )
				{
					array2[ bs.nextClearBit( 0 ) ] = s;
					if( Arrays.stream( array2 ).allMatch( ( str ) -> str != null ) )
					{
						p( "--2--" );
						flag2[ 0 ] = true;
					}
				}
			}
			*/

			if( flag1[ 0 ] )//&& flag2[ 0 ] )
			{
				break;
			}
		}
		return i;
	};

	IntStream.range( 0, 8 ).parallel().map( i -> func.apply( i ) ).toArray();

	print = true;
	for( String s2 : array1 )
	{
		p( s2 );
	}

	for( String s2 : array1 )
	{
		p( "--------------------------------" );
		p( "Testing >>> " + s2 );
		for( Function< String, Boolean > f : fList )
		{
			f.apply( s2 );
		}
	}
}

public static String getStr1()
{
	Random r = new Random();
	int length = r.nextInt( 18 ) + 1;
	StringBuilder buf = new StringBuilder();
	for( int i = 0; i < length; ++i )
	{
		buf.append( charList.get( r.nextInt( charList.size() ) ) );
	}
	return buf.toString();
}

public static void p( Object o )
{
	if( print )
	{
		System.out.println( o );
	}
}
}