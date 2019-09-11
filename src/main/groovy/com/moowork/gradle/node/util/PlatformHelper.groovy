package com.moowork.gradle.node.util

class PlatformHelper
{
    static PlatformHelper INSTANCE = new PlatformHelper()

    private final Properties props;

    public PlatformHelper()
    {
        this( System.getProperties() )
    }

    public PlatformHelper( final Properties props )
    {
        this.props = props;
    }

    private String property( final String name )
    {
        def value = this.props.getProperty( name )
        return value != null ? value : System.getProperty( name )
    }

    public String getOsName()
    {
        final String name = property( "os.name" ).toLowerCase()
        if ( name.contains( "windows" ) )
        {
            return "win"
        }

        if ( name.contains( "mac" ) )
        {
            return "darwin"
        }

        if ( name.contains( "linux" ) )
        {
            return "linux"
        }

        if ( name.contains( "freebsd" ) )
        {
            return "linux"
        }

        if ( name.contains( "sunos" ) )
        {
            return "sunos"
        }

        throw new IllegalArgumentException( "Unsupported OS: " + name )
    }

    public String getOsArch()
    {
        final String arch = property( "os.arch" ).toLowerCase()
        //as Java just returns "arm" on all ARM variants, we need a system call to determine the exact arch
        //unfortunately some JVMs say aarch32/64, so we need an additional conditional
        if( arch.equals( "arm" ) || arch.startsWith( "aarch" ))
        {
            def systemArch = 'uname -m'.execute().text.trim()
            //the node binaries for 'armv8l' are called 'arm64', so we need to distinguish here
            if(systemArch.equals("armv8l"))
            {
                return "arm64"
            }
            else
            {
                return systemArch
            }
        }
        else if ( arch.contains( "64" ) )
        {
            return "x64"
        }

        return "x86"
    }

    public boolean isWindows()
    {
        return getOsName().equals( "win" )
    }
}
