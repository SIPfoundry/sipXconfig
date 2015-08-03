# Initial Version Copyright (C) 2010 eZuce, Inc., All Rights Reserved.
# Licensed to the User under the LGPL license.

# sipXecs projects that are essential for a running communication system.
# Order is important, projects that are dependant on other project
# should be listed after it's dependencies.  No circular dependecies
# allowed.
sipx_core = \
  sipXsupervisor \
  sipXcommons \
  sipXcdr \
  sipXconfig \
  sipXviewer \
  sipXrest \
  sipXcdrLog \
  sipXcallController \
  sipXprovision \
  sipXrelease \
  sipXecs


sipx_all = $(sipx_core) 


# Project compile-time dependencies. Only list project that if
# it's dependecies were recompiled then you'd want to recompile.
sipXcdr_DEPS = sipXcommons
sipXconfig_DEPS = sipXcommons sipXcdr
sipXpolycom_DEPS = sipXconfig

all = $(sipx)
