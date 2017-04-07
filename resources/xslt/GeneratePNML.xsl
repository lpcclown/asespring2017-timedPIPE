<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

   <xsl:output method="xml" encoding="iso-8859-1" omit-xml-declaration="no" indent="yes" version="1.0"/>

   <xsl:template match="/">
      <xsl:element name="pnml">
         <xsl:apply-templates select="pnml"/>
      </xsl:element>
   </xsl:template>
   <xsl:template match="pnml">
      <xsl:element name="net">
         <xsl:attribute name="id">
            <xsl:value-of select="net/@id"/>
         </xsl:attribute>
         <xsl:attribute name="type">
            <xsl:value-of select="net/@type"/>
         </xsl:attribute>
         <xsl:apply-templates select="net/labels">
            <xsl:sort select="@text" data-type="text"/>
         </xsl:apply-templates>
         <xsl:apply-templates select="net/definitions">
            <xsl:sort select="@text" data-type="text"/>
         </xsl:apply-templates>         
         <xsl:apply-templates select="net/place">
            <xsl:sort select="@id" data-type="text"/>
         </xsl:apply-templates>
         <xsl:apply-templates select="net/sysPlace">
            <xsl:sort select="@id" data-type="text"/>
         </xsl:apply-templates>
         <xsl:apply-templates select="net/transition">
            <xsl:sort select="@id" data-type="text"/>
         </xsl:apply-templates>
         <xsl:apply-templates select="net/arc">
            <xsl:sort select="@id" data-type="text"/>
         </xsl:apply-templates>
         <xsl:apply-templates select="net/stategroup">
            <xsl:sort select="@id" data-type="text"/>
         </xsl:apply-templates>         
      </xsl:element>
   </xsl:template>
 
 
 
   
   <xsl:template match="net/sysPlace">
	  <xsl:element name="sysPlace">
   	  <xsl:call-template name="place-transition"/>
	  <xsl:for-each select = "agentToken">
      <xsl:call-template name="agentTokenTemplate"/>
	  </xsl:for-each>
      </xsl:element>
   </xsl:template>
   
   <xsl:template name="agentTokenTemplate">
   	  <xsl:element name="agentToken">
	   	<xsl:attribute name="name">
	   	  	<xsl:value-of select="@name"/>
	   	</xsl:attribute>
		<xsl:call-template name = "agentNetTemplate"/>
   	  </xsl:element>
   </xsl:template>
   
   <xsl:template name="agentNetTemplate">
   	  <xsl:element name="agentNet">
   	  <xsl:apply-templates select="agentNet/labels">
            <xsl:sort select="@text" data-type="text"/>
         </xsl:apply-templates>
         <xsl:apply-templates select="agentNet/definitions">
            <xsl:sort select="@text" data-type="text"/>
         </xsl:apply-templates>         
         <xsl:apply-templates select="agentNet/place">
            <xsl:sort select="@id" data-type="text"/>
         </xsl:apply-templates>
         <xsl:apply-templates select="agentNet/transition">
            <xsl:sort select="@id" data-type="text"/>
         </xsl:apply-templates>
         <xsl:apply-templates select="agentNet/arc">
            <xsl:sort select="@id" data-type="text"/>
         </xsl:apply-templates>
         <xsl:apply-templates select="agentNet/stategroup">
            <xsl:sort select="@id" data-type="text"/>
         </xsl:apply-templates>         
   	  </xsl:element>
   </xsl:template>
   
   <xsl:template match="net/place">
      <xsl:element name="place">
         <xsl:call-template name="place-transition"/>
         <xsl:call-template name="initialMarking"/>
         <xsl:call-template name="capacity"/>
         <xsl:call-template name="placeMarkingParameter"/>  
		 <xsl:call-template name="DataType"/>
		 <xsl:element name = "Datatype-group">
			<xsl:for-each select = "group">
				<xsl:call-template name = "group-DataType"/>
			</xsl:for-each>
		 </xsl:element>	
		 <xsl:call-template name = "abtokentemplate"/>
      </xsl:element>
   </xsl:template>
   
      <xsl:template match="agentNet/place">
      <xsl:element name="place">
         <xsl:call-template name="place-transition"/>
         <xsl:call-template name="initialMarking"/>
         <xsl:call-template name="capacity"/>
         <xsl:call-template name="placeMarkingParameter"/>  
		 <xsl:call-template name="DataType"/>
		 <xsl:element name = "Datatype-group">
			<xsl:for-each select = "group">
				<xsl:call-template name = "group-DataType"/>
			</xsl:for-each>
		 </xsl:element>	
		 <xsl:call-template name = "abtokentemplate"/>
      </xsl:element>
   </xsl:template>
   
   <xsl:template name = "abtokentemplate">
	  <xsl:element name = "abtoken">
		<xsl:element name = "isDef">
			<xsl:element name = "value">
				<xsl:value-of select = "@isDef"/>
			</xsl:element>
		</xsl:element>
		<xsl:element name = "listToken">
			<xsl:for-each select = "listToken">
				<xsl:call-template name = "listTokentemplate"/>
			</xsl:for-each>
		</xsl:element>
	  </xsl:element>
   </xsl:template>
   
   <xsl:template name = "listTokentemplate">
	  <xsl:element name = "token">
		<xsl:for-each select = "Tlist">
			<xsl:element name = "data">
				<xsl:element name = "value">
					<xsl:value-of select = "@data"/>
				</xsl:element>
			</xsl:element>
		</xsl:for-each>
		</xsl:element>
   </xsl:template>
   
     <xsl:template name = "group-DataType">
		<xsl:element name = "DataType">
			<xsl:element name = "name">
				<xsl:element name = "value">
					<xsl:value-of select = "@group-dt"/>
				</xsl:element>
			</xsl:element>
			<xsl:element name = "Ntype">
				<xsl:element name = "value">
					<xsl:value-of select = "@group-ntype"/>
				</xsl:element>
			</xsl:element>
			<xsl:element name = "types">
				<xsl:element name = "value">
					<xsl:value-of select = "@group-types"/>
				</xsl:element>
			</xsl:element>
			<xsl:element name = "ifPow">
				<xsl:element name = "value">
					<xsl:value-of select = "@group-ifpow"/>
				</xsl:element>
			</xsl:element>
			<xsl:element name = "NumofElement">
				<xsl:element name = "value">
					<xsl:value-of select = "@group-numofelement"/>
				</xsl:element>
			</xsl:element>
			<xsl:element name = "isDef">
				<xsl:element name = "value">
					<xsl:value-of select = "@group-ifdef"/>
				</xsl:element>
			</xsl:element>
		</xsl:element>
   </xsl:template>
   
   <xsl:template name = "DataType">
		<xsl:element name = "DataType">
			<xsl:element name = "name">
				<xsl:element name = "value">
					<xsl:value-of select = "@dt"/>
				</xsl:element>
			</xsl:element>
			<xsl:element name = "Ntype">
				<xsl:element name = "value">
					<xsl:value-of select = "@ntype"/>
				</xsl:element>
			</xsl:element>
			<xsl:element name = "types">
				<xsl:element name = "value">
					<xsl:value-of select = "@types"/>
				</xsl:element>
			</xsl:element>
			<xsl:element name = "ifPow">
				<xsl:element name = "value">
					<xsl:value-of select = "@ifpow"/>
				</xsl:element>
			</xsl:element>
			<xsl:element name = "NumofElement">
				<xsl:element name = "value">
					<xsl:value-of select = "@numofelement"/>
				</xsl:element>
			</xsl:element>
			<xsl:element name = "isDef">
				<xsl:element name = "value">
					<xsl:value-of select = "@ifdef"/>
				</xsl:element>
			</xsl:element>
		</xsl:element>
   </xsl:template>
   
   <xsl:template match="net/labels">
      <xsl:element name="labels">
         <xsl:attribute name="x">
            <xsl:value-of select="@positionX"/>
         </xsl:attribute>
         <xsl:attribute name="y">
            <xsl:value-of select="@positionY"/>
         </xsl:attribute>
         <xsl:attribute name="width">
            <xsl:value-of select="@width"/>
         </xsl:attribute>
         <xsl:attribute name="height">
            <xsl:value-of select="@height"/>
         </xsl:attribute>
         <xsl:attribute name="border">
            <xsl:value-of select="@border"/>
         </xsl:attribute>
         <xsl:element name="text">
            <xsl:value-of select="@text"/>
         </xsl:element>
      </xsl:element>
   </xsl:template>
   
   <xsl:template match="agentNet/labels">
      <xsl:element name="labels">
         <xsl:attribute name="x">
            <xsl:value-of select="@positionX"/>
         </xsl:attribute>
         <xsl:attribute name="y">
            <xsl:value-of select="@positionY"/>
         </xsl:attribute>
         <xsl:attribute name="width">
            <xsl:value-of select="@width"/>
         </xsl:attribute>
         <xsl:attribute name="height">
            <xsl:value-of select="@height"/>
         </xsl:attribute>
         <xsl:attribute name="border">
            <xsl:value-of select="@border"/>
         </xsl:attribute>
         <xsl:element name="text">
            <xsl:value-of select="@text"/>
         </xsl:element>
      </xsl:element>
   </xsl:template>

   <xsl:template match="net/definitions">
      <xsl:element name="definition">
         <xsl:attribute name="defType">
            <xsl:value-of select="@defType"/>
         </xsl:attribute>
         <xsl:attribute name="expression">
            <xsl:value-of select="@expression"/>
         </xsl:attribute>
         <xsl:attribute name="id">
            <xsl:value-of select="@id"/>
         </xsl:attribute>
         <xsl:attribute name="name">
            <xsl:value-of select="@name"/>
         </xsl:attribute>
         <xsl:attribute name="type">
            <xsl:value-of select="@type"/>
         </xsl:attribute>
         <xsl:call-template name="graphics"/>
      </xsl:element>
   </xsl:template>   
   
   <xsl:template match="agentNet/definitions">
      <xsl:element name="definition">
         <xsl:attribute name="defType">
            <xsl:value-of select="@defType"/>
         </xsl:attribute>
         <xsl:attribute name="expression">
            <xsl:value-of select="@expression"/>
         </xsl:attribute>
         <xsl:attribute name="id">
            <xsl:value-of select="@id"/>
         </xsl:attribute>
         <xsl:attribute name="name">
            <xsl:value-of select="@name"/>
         </xsl:attribute>
         <xsl:attribute name="type">
            <xsl:value-of select="@type"/>
         </xsl:attribute>
         <xsl:call-template name="graphics"/>
      </xsl:element>
   </xsl:template> 
   
   <xsl:template match="net/transition">
      <xsl:element name="transition">
         <xsl:call-template name="place-transition"/>
         <xsl:call-template name="orientation"/>
         <xsl:call-template name="myrate"/>
         <xsl:call-template name="timed"/>
         <xsl:call-template name="infinite-server"/>
         <xsl:call-template name="priority"/>         
         <xsl:call-template name="transitionRateParameter"/>
         <xsl:call-template name="formula"/>
      </xsl:element>
   </xsl:template>
   
   <xsl:template match="agentNet/transition">
      <xsl:element name="transition">
         <xsl:call-template name="place-transition"/>
         <xsl:call-template name="orientation"/>
         <xsl:call-template name="myrate"/>
         <xsl:call-template name="timed"/>
         <xsl:call-template name="infinite-server"/>
         <xsl:call-template name="priority"/>         
         <xsl:call-template name="transitionRateParameter"/>
         <xsl:call-template name="formula"/>
      </xsl:element>
   </xsl:template>
   
   <xsl:template name="place-transition">
      <xsl:attribute name="id">
         <xsl:value-of select="@id"/>
      </xsl:attribute>
      <xsl:call-template name="graphics"/>
      <xsl:call-template name="name"/>
   </xsl:template>
   
   <xsl:template match="net/arc">
      <xsl:element name="arc">
         <xsl:attribute name="id">
            <xsl:value-of select="@id"/>
         </xsl:attribute>
         <xsl:attribute name="source">
            <xsl:value-of select="@source"/>
         </xsl:attribute>
         <xsl:attribute name="target">
            <xsl:value-of select="@target"/>
         </xsl:attribute>
		 <xsl:attribute name = "variable">
			<xsl:value-of select="@var"/>
		 </xsl:attribute>
         <xsl:call-template name="graphics"/>
         <xsl:call-template name="inscription"/>
         <xsl:call-template name="tagged"/>
		 <xsl:call-template name = "DataType"/>
         <xsl:apply-templates select="arcpath">
            <xsl:sort select="@id" data-type="text"/>
         </xsl:apply-templates>
         <xsl:element name="type">
            <xsl:attribute name="value">
               <xsl:value-of select="@type"/>
            </xsl:attribute>
         </xsl:element>
      </xsl:element>
   </xsl:template>
   
   <xsl:template match="agentNet/arc">
      <xsl:element name="arc">
         <xsl:attribute name="id">
            <xsl:value-of select="@id"/>
         </xsl:attribute>
         <xsl:attribute name="source">
            <xsl:value-of select="@source"/>
         </xsl:attribute>
         <xsl:attribute name="target">
            <xsl:value-of select="@target"/>
         </xsl:attribute>
		 <xsl:attribute name = "variable">
			<xsl:value-of select="@var"/>
		 </xsl:attribute>
         <xsl:call-template name="graphics"/>
         <xsl:call-template name="inscription"/>
         <xsl:call-template name="tagged"/>
		 <xsl:call-template name = "DataType"/>
         <xsl:apply-templates select="arcpath">
            <xsl:sort select="@id" data-type="text"/>
         </xsl:apply-templates>
         <xsl:element name="type">
            <xsl:attribute name="value">
               <xsl:value-of select="@type"/>
            </xsl:attribute>
         </xsl:element>
      </xsl:element>
   </xsl:template>
   
   <xsl:template match="arcpath">
      <xsl:element name = "arcpath">
         <xsl:attribute name = "id">
            <xsl:value-of select ="@id"/>
         </xsl:attribute>
         <xsl:attribute name = "x">
            <xsl:value-of select ="@xCoord"/>
         </xsl:attribute>
         <xsl:attribute name = "y">
            <xsl:value-of select ="@yCoord"/>
         </xsl:attribute>
         <xsl:attribute name = "curvePoint">
            <xsl:value-of select="@arcPointType"/>
         </xsl:attribute>
      </xsl:element>
   </xsl:template>
   
   <xsl:template match="net/stategroup">
      <xsl:element name="stategroup">
         <xsl:attribute name="id">
            <xsl:value-of select="@id"/>
         </xsl:attribute>
         <xsl:element name="name">
            <xsl:element name="value">
               <xsl:value-of select="@name"/>
            </xsl:element>
         </xsl:element>
         <xsl:apply-templates select="statecondition">
<!-- 				<xsl:sort select="@text" data-type="text"/> -->
         </xsl:apply-templates>
      </xsl:element>
   </xsl:template>
   
   <xsl:template match="agentNet/stategroup">
      <xsl:element name="stategroup">
         <xsl:attribute name="id">
            <xsl:value-of select="@id"/>
         </xsl:attribute>
         <xsl:element name="name">
            <xsl:element name="value">
               <xsl:value-of select="@name"/>
            </xsl:element>
         </xsl:element>
         <xsl:apply-templates select="statecondition">
<!-- 				<xsl:sort select="@text" data-type="text"/> -->
         </xsl:apply-templates>
      </xsl:element>
   </xsl:template>
   
   <xsl:template match="statecondition">
      <xsl:element name = "statecondition">
         <xsl:element name="value">
            <xsl:value-of select="@condition"/>
         </xsl:element>
      </xsl:element>
   </xsl:template>   
   
   <xsl:template name="graphics">
      <xsl:element name="graphics">
         <xsl:if test="(string-length(@positionX) > 0) and (string-length(@positionY) > 0)">
            <xsl:element name="position">
               <xsl:attribute name="x">
                  <xsl:value-of select="@positionX"/>
               </xsl:attribute>
               <xsl:attribute name="y">
                  <xsl:value-of select="@positionY"/>
               </xsl:attribute>
            </xsl:element>
         </xsl:if>
      </xsl:element>
   </xsl:template>
   
   <xsl:template name="name">
      <xsl:element name="name">
         <xsl:element name="value">
            <xsl:value-of select="@name"/>
         </xsl:element>
         <xsl:element name="graphics">
            <xsl:if test="(string-length(@nameOffsetX) > 0)and (string-length(@nameOffsetY) > 0)">
               <xsl:element name="offset">
                  <xsl:attribute name="x">
                     <xsl:value-of select="@nameOffsetX"/>
                  </xsl:attribute>
                  <xsl:attribute name="y">
                     <xsl:value-of select="@nameOffsetY"/>
                  </xsl:attribute>
               </xsl:element>
            </xsl:if>
         </xsl:element>
      </xsl:element>
   </xsl:template>
   
   <xsl:template name="initialMarking">
      <xsl:element name="initialMarking">
         <xsl:element name="value">
            <xsl:value-of select="@initialMarking"/>
         </xsl:element>
         <xsl:element name="graphics">
            <xsl:if test="(string-length(@markingOffsetX) > 0) and (string-length(@markingOffsetY) > 0)">
               <xsl:element name="offset">
                  <xsl:attribute name="x">
                     <xsl:value-of select="@markingOffsetX"/>
                  </xsl:attribute>
                  <xsl:attribute name="y">
                     <xsl:value-of select="@markingOffsetY"/>
                  </xsl:attribute>
               </xsl:element>
            </xsl:if>
         </xsl:element>
      </xsl:element>
   </xsl:template>
   
   <xsl:template name="inscription">
      <xsl:element name="inscription">
         <xsl:element name="value">
            <xsl:value-of select="@inscription"/>
         </xsl:element>
         <xsl:element name="graphics">
            <xsl:if test="(string-length(@inscriptionOffsetX) > 0)  and (string-length(@inscriptionOffsetY) > 0)">
               <xsl:element name="offset">
                  <xsl:attribute name="x">
                     <xsl:value-of select="@inscriptionOffsetX"/>
                  </xsl:attribute>
                  <xsl:attribute name="y">
                     <xsl:value-of select="@inscriptionOffsetY"/>
                  </xsl:attribute>
               </xsl:element>
            </xsl:if>
         </xsl:element>
      </xsl:element>
   </xsl:template>
   
   <xsl:template name="myrate">
      <xsl:element name = "rate">
         <xsl:element name = "value">
            <xsl:value-of select="@rate"/>
         </xsl:element>
      </xsl:element>
   </xsl:template>
   
   <xsl:template name="timed">
      <xsl:element name = "timed">
         <xsl:element name = "value">
            <xsl:value-of select="@timed"/>
         </xsl:element>
      </xsl:element>
   </xsl:template>

   <xsl:template name="infinite-server">
      <xsl:element name = "infiniteServer">
         <xsl:element name = "value">
            <xsl:value-of select="@infiniteServer"/>
         </xsl:element>
      </xsl:element>
   </xsl:template>   
   
   <xsl:template name="orientation">
      <xsl:element name = "orientation">
         <xsl:element name = "value">
            <xsl:value-of select="@angle"/>
         </xsl:element>
      </xsl:element>
   </xsl:template>
   
   <xsl:template name="priority">
      <xsl:element name = "priority">
         <xsl:element name = "value">
            <xsl:value-of select="@priority"/>
         </xsl:element>
      </xsl:element>
   </xsl:template>
   
   <xsl:template  name="tagged">
      <xsl:element name = "tagged">
         <xsl:element name = "value">
            <xsl:value-of select="@tagged"/>
         </xsl:element>
      </xsl:element>
   </xsl:template>
   
   
   <xsl:template name="weight">
      <xsl:element name = "weight">
         <xsl:element name = "value">
            <xsl:value-of select="@weight"/>
         </xsl:element>
      </xsl:element>
   </xsl:template>        
   
   <xsl:template name="capacity">
      <xsl:element name = "capacity">
         <xsl:element name = "value">
            <xsl:value-of select="@capacity"/>
         </xsl:element>
      </xsl:element>
   </xsl:template>  

   <xsl:template name="transitionRateParameter">
      <xsl:if test="string-length(@parameter)>0">
         <xsl:element name="toolspecific">
            <xsl:attribute name="tool">
               <xsl:text>PIPE</xsl:text>
            </xsl:attribute>
            <xsl:attribute name="version">
               <xsl:text>2.5</xsl:text>
            </xsl:attribute>           
            <xsl:attribute name="rateDefinition">
               <xsl:value-of select="@parameter"/>
            </xsl:attribute>            
<!--            <xsl:element name="name">
               <xsl:element name="value">
                  <xsl:value-of select="@parameter"/>
               </xsl:element>         
            </xsl:element>         -->
         </xsl:element>  
      </xsl:if>
   </xsl:template>   
   
   <xsl:template name="placeMarkingParameter">
      <xsl:if test="string-length(@parameter)>0">
         <xsl:element name="toolspecific">
            <xsl:attribute name="tool">
               <xsl:text>PIPE</xsl:text>
            </xsl:attribute>
            <xsl:attribute name="version">
               <xsl:text>2.5</xsl:text>
            </xsl:attribute>  
            <xsl:attribute name="markingDefinition">
               <xsl:value-of select="@parameter"/>
            </xsl:attribute>
<!--            <xsl:element name="name">
               <xsl:element name="value">
                  <xsl:value-of select="@parameter"/>
               </xsl:element>         
            </xsl:element>         -->
         </xsl:element>  
      </xsl:if>
   </xsl:template> 


   <xsl:template name="formula">
      <xsl:element name = "formula">
      	<xsl:element name = "value">
       		 <xsl:value-of select="@formula"/>
        </xsl:element>
      </xsl:element>
   </xsl:template>  
   
</xsl:stylesheet>