package com.example.aisplitwise.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.aisplitwise.navigation.CreateGroupRoute
import com.example.aisplitwise.navigation.LedgerRoute
import com.example.aisplitwise.data.local.Group
import com.example.aisplitwise.data.local.Member
import com.example.aisplitwise.navigation.JoinGroupDialogRoute
import com.example.aisplitwise.utils.ifNullOrEmpty
import com.google.firebase.Timestamp
import java.util.Date


@Composable
fun DashBoard(dashBoardViewModel: DashboardViewModel, navController: NavHostController) {
    val uiState by dashBoardViewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(modifier=Modifier,topBar = { DashboardHeader(uiState.member) }) { padding ->
        DashBoardContent(
            modifier = Modifier
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .safeContentPadding(),
            {navController.navigate(CreateGroupRoute)},
            dashBoardViewModel::getGroupsApiCall,
            uiState.groupList,
            navigateGroup = {navController.navigate(LedgerRoute(it))},
            navController=navController
        )



    }
    if(uiState.showLoader) {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f))) {
            CircularProgressIndicator(
                Modifier
                    .size(64.dp)
                    .align(Alignment.Center),
                strokeWidth =8.dp )
        }
    }

}

@Composable
fun DashBoardContent(
    modifier: Modifier = Modifier,
    createGroup: () -> Unit,
    getGroupsApiCall: () -> Unit,
    groupList: List<Group> = emptyList(),
    navigateGroup: (String) -> Unit,
    navController: NavHostController
) {
    Box(modifier = modifier) {
        Column(Modifier.fillMaxSize()) {


            LazyColumn {
                item{
                    Row(
                        Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Groups",
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .weight(1f),
                            style = MaterialTheme.typography.headlineMedium,
                        )

                        Icon(
                            imageVector = Icons.Rounded.Replay, // Replace with your icon resource
                            contentDescription = "Refresh Group", // Replace with your string resource
                            modifier = Modifier
                                .size(24.dp)
                                .clickable {
                                    getGroupsApiCall.invoke()
                                }
                        )
                    }

                    Row(modifier=Modifier.padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        FilledTonalButton(
                            onClick = {
                                createGroup()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 4.dp)
                        ) {
                            Row (
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceAround
                            ){
                                Text(text = "Create Group")
                                Icon(
                                    imageVector = Icons.Default.MonetizationOn,
                                    contentDescription = "Add Expense",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        FilledTonalButton(
                            onClick = {
                                navController.navigate(JoinGroupDialogRoute(""))
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 4.dp),
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceAround) {
                                Text(text = "Add Member")
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                items(groupList){
                    GroupCard(it){
                        navigateGroup.invoke(it.id)
                    }
                }

            }


        }
    }

}


@Composable
fun DashboardHeader(
    route: Member?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),

    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val context= LocalContext.current

            SubcomposeAsyncImage(model = ImageRequest.Builder(context)
                .data(route?.photoUrl ?: "")
                .crossfade(true)
                .build(), contentDescription = "",
                loading = {
                    Icon(
                        imageVector = Icons.Rounded.AccountCircle, // Replace with your icon resource
                        contentDescription ="Create Group", // Replace with your string resource
                        modifier = Modifier.size(64.dp)
                    )
                },
                error = { Icon(
                    imageVector = Icons.Rounded.AccountCircle, // Replace with your icon resource
                    contentDescription ="Create Group", // Replace with your string resource
                    modifier = Modifier.size(64.dp)
                )})

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = route?.displayName ?: "No Name",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = route?.email ?: "No Email",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text =route?.phoneNumber?.ifNullOrEmpty { "No Phone Number" }?:"No Phone Number",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardHeaderPreview() {
    Box(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f))) {
        CircularProgressIndicator(
            Modifier
                .size(64.dp)
                .align(Alignment.Center),
            strokeWidth =8.dp )
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardContentPreview() {
    Surface(color = Color.White) {

        DashBoardContent(
            Modifier,
            createGroup = {},
            getGroupsApiCall = {},
            groupList = listOf(
                Group(
                    id = "group1",
             name = "Weekend Getaway",
             members = emptyList(),
             createdAt = Timestamp(Date()),
             updatedAt = Timestamp(Date()),
             groupImg = "https://example.com/sample-group-img.jpg" // Replace with a valid image URL
         )
            ),
            navigateGroup = {},
            navController = rememberNavController()
        )
    }
}
